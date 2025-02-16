package predictor.demo.AppModules.user;

import predictor.demo.Error.AppException;
import java.util.List;

public interface UserService {
    User createUser(User user) throws AppException;
    User getUserById(int id) throws AppException;
    User getUserByEmail(String email) throws AppException;
    User getUserByGoogleSubject(String googleSubject) throws AppException;
    void updateUser(User user) throws AppException;
    void deleteUser(int id) throws AppException;
    List<User> getAllUsers();
    User findOrCreateUser(String email, String googleSubject, String refreshToken) throws AppException;
    void updateRefreshToken(int userId, String refreshToken) throws AppException;
}