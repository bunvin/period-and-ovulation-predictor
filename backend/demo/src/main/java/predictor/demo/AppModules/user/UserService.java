package predictor.demo.AppModules.user;

import predictor.demo.Error.AppException;
import java.util.List;

import org.springframework.security.oauth2.core.user.OAuth2User;

public interface UserService {
    User getCurrentUser(OAuth2User principal);
    User createUser(User user) throws AppException;
    User getUserById(int id) throws AppException;
    User getUserByEmail(String email) throws AppException;
    User getUserByGoogleSubject(String googleSubject) throws AppException;
    void updateUser(User user, int userId) throws AppException;
    void deleteUser(int id) throws AppException;
    List<User> getAllUsers();
    void updateRefreshToken(int userId, String refreshToken) throws AppException;

    User findOrCreateUser(String email, String googleSubject, String refreshToken) throws Exception;
}