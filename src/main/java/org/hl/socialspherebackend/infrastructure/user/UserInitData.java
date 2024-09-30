package org.hl.socialspherebackend.infrastructure.user;

import com.github.javafaker.Faker;
import org.hl.socialspherebackend.api.entity.user.*;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserInitData implements InitializingBean {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserInitData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(userRepository.count() > 0) {
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
        Instant now = Instant.now();
        User user1 = new User("user", passwordEncoder.encode("test"), now);
        user1.appendAuthority(new Authority(user1, "USER"));
        User user2 = new User("user2", passwordEncoder.encode("test"), now);
        user1.appendAuthority(new Authority(user1, "USER"));
        userRepository.save(user1);
        userRepository.save(user2);
        for (int i = 0; i < 48; i++) {
            String username = faker.name().username();
            String password = passwordEncoder.encode(faker.internet().password());
            Instant now2 = Instant.now();
            User user = new User(username, password, now2);
            user.appendAuthority(new Authority(user, "USER"));
            userRepository.save(user);
        }
    }

    private void createAuthorities() {
        List<User> users = userRepository.findAll();
        users.forEach((u) -> {
            Authority authority = new Authority(u, "USER");
            u.appendAuthority(authority);
            userRepository.save(u);
        });
    }

    private void createUserProfiles() {
        Faker faker = new Faker();
        List<User> users = userRepository.findAll();

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
            userRepository.save(u);
        });
    }


    private void createUserProfilePictures() throws IOException {
        List<User> users = userRepository.findAll();

        for (User u : users) {
            UserProfile up = u.getUserProfile();
            String imageType = "image/png";
            byte[] compressedImg = FileUtils.compressFile(generateRandomImage());
            UserProfilePicture userProfilePicture = new UserProfilePicture(imageType, compressedImg);
            up.setProfilePicture(userProfilePicture);
            u.setUserProfile(up);
            userRepository.save(u);
        }
    }

    private void createUserProfileConfigs() {
        Faker faker = new Faker();
        List<User> users = userRepository.findAll();

        users.forEach((u) -> {
            UserProfilePrivacyLevel privacyLevel = faker.options().option(UserProfilePrivacyLevel.class);
            UserProfileConfig userProfileConfig = new UserProfileConfig(privacyLevel, u);
            u.setUserProfileConfig(userProfileConfig);
            userRepository.save(u);
        });
    }


    private void assignRandomFriends() {
        Faker faker = new Faker();
        List<User> users = userRepository.findAll();
        List<Long> userIds = new ArrayList<>();
        for (User user : users) {
            Long userId = user.getId();
            long randomId = faker.number().numberBetween(0, 50);
            if(userIds.contains(randomId) || userIds.contains(userId)) continue;

            userIds.add(userId);
            userIds.add(randomId);
            User randomUser = users.get((int) randomId);
            if(!user.equals(randomUser)) {
                user.appendFriend(randomUser);
                userRepository.save(user);
            }
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
