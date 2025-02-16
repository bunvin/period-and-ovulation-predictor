package predictor.demo.AppModules.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import predictor.demo.AppModules.eventsSeries.EventsSeries;

import java.time.LocalDateTime;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "google_sub", unique = true)
    private String googleSubject;

    @Column(name = "refresh_token")
    private String refreshToken;

    private String name; //from google
    private String picture; //from google
    private boolean isActive = true;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<EventsSeries> eventsSeriesList;

    @Column(updatable = false)
    private final LocalDateTime createdDateTime = LocalDateTime.now();
    private LocalDateTime modifiedDateTime = LocalDateTime.now();

    public User() {
    }

    public User(String picture, String name, String refreshToken, String googleSubject, String email, int id) {
        this.picture = picture;
        this.name = name;
        this.refreshToken = refreshToken;
        this.googleSubject = googleSubject;
        this.email = email;
        this.id = id;
    }

    @PreUpdate
    public void updateModifiedDateTime() {
        this.modifiedDateTime = LocalDateTime.now();
    }

    //builder
    // Builder class
    public static class UserBuilder {
        private int id;
        private String email;
        private String googleSubject;
        private String refreshToken;
        private String name;
        private String picture;
        private boolean isActive;

        // Fixed return types to enable method chaining
        public UserBuilder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder picture(String picture) {
            this.picture = picture;
            return this;
        }

        public UserBuilder id(int id) {
            this.id = id;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder googleSubject(String googleSubject) {
            this.googleSubject = googleSubject;
            return this;
        }

        public UserBuilder refreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        public User build() {
            User user = new User();
            user.id = this.id;
            user.email = this.email;
            user.googleSubject = this.googleSubject;
            user.refreshToken = this.refreshToken;
            user.name = this.name;
            user.picture = this.picture;
            user.isActive = this.isActive;
            return user;
        }
    }

    //getter setter toString


    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getGoogleSubject() {
        return googleSubject;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public LocalDateTime getModifiedDateTime() {
        return modifiedDateTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGoogleSubject(String googleSubject) {
        this.googleSubject = googleSubject;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", googleSubject='" + googleSubject + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
