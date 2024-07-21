package org.hl.socialspherebackend.infrastructure.user;

import com.github.javafaker.Faker;
import org.hl.socialspherebackend.api.entity.user.*;
import org.hl.socialspherebackend.application.user.UserFacade;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UserInitData implements InitializingBean {

    private final UserFacade userFacade;
    private final PasswordEncoder passwordEncoder;

    public UserInitData(UserFacade userFacade, PasswordEncoder passwordEncoder) {
        this.userFacade = userFacade;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(userFacade.countUserEntities() > 1) {
           return;
       }
       createUsers();
       createAuthorities();
       createUserProfiles();
       createUserProfilePictures();
       createUserProfileConfigs();
       assignRandomFriends();
    }


    private void createUsers() {
        Faker faker = new Faker();
        User user1 = new User("user", passwordEncoder.encode("test"));
        user1.appendAuthority(new Authority(user1, "USER"));
        User user2 = new User("user2", passwordEncoder.encode("test"));
        user1.appendAuthority(new Authority(user1, "USER"));
        userFacade.saveUserEntity(user1);
        userFacade.saveUserEntity(user2);
        for (int i = 0; i < 48; i++) {
            String username = faker.name().username();
            String password = passwordEncoder.encode(faker.internet().password());
            User user = new User(username, password);
            user.appendAuthority(new Authority(user, "USER"));
            userFacade.saveUserEntity(user);
        }
    }

    private void createAuthorities() {
        List<User> users = userFacade.findAllUserEntities();
        users.forEach((u) -> {
            Authority authority = new Authority(u, "USER");
            u.appendAuthority(authority);
            userFacade.saveUserEntity(u);
        });
    }

    private void createUserProfiles() {
        Faker faker = new Faker();
        List<User> users = userFacade.findAllUserEntities();

        users.forEach((u) -> {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            String city = faker.address().city();
            String country = faker.address().country();
            UserProfile userProfile = new UserProfile(
                    firstName,
                    lastName,
                    city,
                    country,
                    u
            );
            u.setUserProfile(userProfile);
            userFacade.saveUserEntity(u);
        });
    }


    private void createUserProfilePictures() throws IOException {
        List<User> users = userFacade.findAllUserEntities();

        for (User u : users) {
            UserProfile up = u.getUserProfile();
            String imageType = "image/png";
            byte[] compressedImg = FileUtils.compressFile(generateRandomImage());
            UserProfilePicture userProfilePicture = new UserProfilePicture(imageType, compressedImg);
            up.setProfilePicture(userProfilePicture);
            u.setUserProfile(up);
            userFacade.saveUserEntity(u);
        }
    }

    private void createUserProfileConfigs() {
        Faker faker = new Faker();
        List<User> users = userFacade.findAllUserEntities();

        users.forEach((u) -> {
            UserProfilePrivacyLevel privacyLevel = faker.options().option(UserProfilePrivacyLevel.class);
            UserProfileConfig userProfileConfig = new UserProfileConfig(privacyLevel, u);
            u.setUserProfileConfig(userProfileConfig);
            userFacade.saveUserEntity(u);
        });
    }


    private void assignRandomFriends() {
        Faker faker = new Faker();
        List<User> users = userFacade.findAllUserEntities();
        for (User user : users) {
            Set<User> friends = IntStream.range(0, faker.number().numberBetween(1, 10))
                    .mapToObj(i -> users.get(faker.number().numberBetween(0, 50)))
                    .collect(Collectors.toSet());
            user.setUserFriendList(friends);
            userFacade.saveUserEntity(user);
        }
    }

    private byte[] generateRandomImage() throws IOException {
        int width = 100;
        int height = 100;
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();

        g2d.setColor(new Color(new Random().nextInt(0xFFFFFF)));
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        return baos.toByteArray();
    }

}
