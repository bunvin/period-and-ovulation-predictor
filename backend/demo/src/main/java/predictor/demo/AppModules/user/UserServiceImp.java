package predictor.demo.AppModules.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import predictor.demo.Error.AppException;

import java.util.List;

@Service
@Slf4j
public class UserServiceImp implements UserService {
    @Autowired
    private UserRepository userRepository;

@Override
    public User getCurrentUser(OAuth2User principal) {
        String googleSubject = principal.getAttribute("sub");

        return userRepository.findByGoogleSubject(googleSubject)
                .map(user -> updateExistingUser(user, principal))
                .orElseGet(() -> createNewUser(principal));
    }

    private User updateExistingUser(User user, OAuth2User principal) {
        // Update user information if changed
        boolean isModified = false;
        
        String name = principal.getAttribute("name");
        if (name != null && !name.equals(user.getName())) {
            user.setName(name);
            isModified = true;
        }

        String picture = principal.getAttribute("picture");
        if (picture != null && !picture.equals(user.getPicture())) {
            user.setPicture(picture);
            isModified = true;
        }

        if (isModified) {
            log.info("Updating user information for user: {}", user.getEmail());
            return userRepository.save(user);
        }
        
        return user;
    }

    private User createNewUser(OAuth2User principal) {
        log.info("Creating new user from Google OAuth2 login");
        
        User newUser = new User.UserBuilder()
            .email(principal.getAttribute("email"))
            .googleSubject(principal.getAttribute("sub"))
            .name(principal.getAttribute("name"))
            .picture(principal.getAttribute("picture"))
            .isActive(true)
            .build();

        return userRepository.save(newUser);
    }

    @Override
    public void updateRefreshToken(int userId, String refreshToken) throws AppException {
        User user = getUserById(userId);
        user.setRefreshToken(refreshToken);
        userRepository.save(user);
        log.info("Updated refresh token for user: {}", user.getEmail());
    }

    @Override
    public User createUser(User user) throws AppException {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new AppException(UserError.USER_EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByGoogleSubject(user.getGoogleSubject())) {
            throw new AppException(UserError.USER_GOOGLE_SUBJECT_ALREADY_EXISTS);
        }
        return userRepository.save(user);
    }

    @Override
    public User getUserById(int id) throws AppException {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(UserError.USER_NOT_FOUND));
    }

    @Override
    public User getUserByEmail(String email) throws AppException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(UserError.USER_NOT_FOUND));
    }

    @Override
    public User getUserByGoogleSubject(String googleSubject) throws AppException {
        return userRepository.findByGoogleSubject(googleSubject)
                .orElseThrow(() -> new AppException(UserError.USER_NOT_FOUND));
    }

    @Override
    public void updateUser(User user, int userId) throws AppException {
        User existingUser = getUserById(userId);
        // If email is being changed, check if new email is available
        if (!existingUser.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(user.getEmail())) {
            throw new AppException(UserError.USER_EMAIL_ALREADY_EXISTS);
        }
        userRepository.save(user);
    }

    @Override
    public void deleteUser(int id) throws AppException {
        if (!userRepository.existsById(id)) {
            throw new AppException(UserError.USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findOrCreateUser(String email, String googleSubject, String refreshToken) throws AppException {
        return userRepository.findByGoogleSubject(googleSubject)
                .map(user -> {
                    // Update refresh token if it has changed
                    if (!refreshToken.equals(user.getRefreshToken())) {
                        user.setRefreshToken(refreshToken);
                        return userRepository.save(user);
                    }
                    return user;
                })
                .orElseGet(() -> {
                    User newUser = new User.UserBuilder()
                            .email(email)
                            .googleSubject(googleSubject)
                            .refreshToken(refreshToken)
                            .build();
                    return userRepository.save(newUser);
                });
    }

}