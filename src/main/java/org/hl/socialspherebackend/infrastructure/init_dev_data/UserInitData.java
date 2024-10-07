package org.hl.socialspherebackend.infrastructure.init_dev_data;

import com.github.javafaker.Faker;
import org.hl.socialspherebackend.api.entity.user.*;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.hl.socialspherebackend.infrastructure.user.UserFriendRequestRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class UserInitData {

    private final UserRepository userRepository;
    private final UserFriendRequestRepository userFriendRequestRepository;
    private final PasswordEncoder passwordEncoder;

    private final String[] femaleFirstNames = {
            "Emma",
            "Olivia",
            "Ava",
            "Isabella",
            "Sophia",
            "Mia",
            "Amelia",
            "Harper",
            "Evelyn",
            "Abigail",
            "Ella",
            "Scarlett",
            "Grace",
            "Chloe",
            "Lily"
    };

    private final String[] maleFirstNames = {
            "Liam",
            "Noah",
            "Oliver",
            "Elijah",
            "James",
            "William",
            "Benjamin",
            "Lucas",
            "Henry",
            "Alexander",
            "Jackson",
            "Sebastian",
            "Mateo",
            "Jack",
            "Owen",
            "Daniel"
    };

    UserInitData(UserRepository userRepository, UserFriendRequestRepository userFriendRequestRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userFriendRequestRepository = userFriendRequestRepository;
        this.passwordEncoder = passwordEncoder;
    }

    void initData() throws IOException {
        if(userRepository.count() > 0) {
            return;
        }
        createUsers();
        createAuthorities();
        createUserProfiles();
        createUserProfilePictures();
        createUserProfileConfigs();
        assignFriends();
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
        for (int i = 0; i < 28; i++) {
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

        for(int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            String firstName;
            if(i >= 15) {
                firstName = femaleFirstNames[i-15];
            } else {
                firstName = maleFirstNames[i];
            }

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
        }

    }


    private void createUserProfilePictures() throws IOException {
        List<User> users = userRepository.findAll();
        String path = "src/main/resources/init_dev_data/user/profile_picture";
        File femaleProfilePicturesDir = new File(path + "/female");
        File maleProfilePicturesDir = new File(path + "/male");

        File[] femaleProfilePictures = femaleProfilePicturesDir.listFiles();
        File[] maleProfilePictures = maleProfilePicturesDir.listFiles();

        for(int i = 0; i < users.size(); i++) {
            File file;
            if(i >= 15) {
                file = femaleProfilePictures[i-15];
            } else {
                file = maleProfilePictures[i];
            }

            BufferedImage bi = ImageIO.read(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bi, "jpg", baos);
            byte[] bytes = baos.toByteArray();

            User u = users.get(i);
            UserProfile up = u.getUserProfile();
            UserProfilePicture usp = new UserProfilePicture("jpg", FileUtils.compressFile(bytes));
            up.setProfilePicture(usp);
            u.setUserProfile(up);
            userRepository.save(u);
        }

    }

    private void createUserProfileConfigs() {
        List<User> users = userRepository.findAll();

        users.forEach((u) -> {
            UserProfilePrivacyLevel privacyLevel = UserProfilePrivacyLevel.PUBLIC;
            UserProfileConfig userProfileConfig = new UserProfileConfig(privacyLevel, u);
            u.setUserProfileConfig(userProfileConfig);
            userRepository.save(u);
        });
    }


    private void assignFriends() {
        User user1 = findUserWithFriendsById(1L);
        User user2 = findUserWithFriendsById(2L);
        User user3 = findUserWithFriendsById(3L);
        User user4 = findUserWithFriendsById(4L);
        User user5 = findUserWithFriendsById(5L);
        User user6 = findUserWithFriendsById(6L);
        User user7 = findUserWithFriendsById(7L);
        User user8 = findUserWithFriendsById(8L);
        User user9 = findUserWithFriendsById(9L);
        User user10 = findUserWithFriendsById(10L);
        User user11 = findUserWithFriendsById(11L);
        User user12 = findUserWithFriendsById(12L);
        User user13 = findUserWithFriendsById(13L);
        User user14 = findUserWithFriendsById(14L);
        User user15 = findUserWithFriendsById(15L);
        User user16 = findUserWithFriendsById(16L);
        User user17 = findUserWithFriendsById(17L);
        appendFriends(user1, user2, user3, user4, user5, user6, user7, user8);
        appendFriends(user2, user3, user4, user5, user6, user7, user8);
        appendFriends(user3, user4, user5, user6, user7, user8);
        appendFriends(user4, user5, user6, user7, user8);
        appendFriends(user5, user6, user7, user8);
        appendFriends(user6, user7, user8);
        appendFriends(user7, user8, user6);
        appendFriends(user8, user6, user7);

        appendFriends(user9, user7, user8, user10, user11, user12,user13, user14, user15, user16);
        appendFriends(user10, user11, user12, user13, user14, user15, user16);
        appendFriends(user11, user12, user13, user14, user15, user16);
        appendFriends(user12, user13, user14, user15, user16, user17);
        appendFriends(user13, user14, user15, user16, user17);
        appendFriends(user14, user15, user16, user17);
        appendFriends(user15, user16, user17);
        appendFriends(user16, user17);

        appendFriendRequest(user1, user9, user10, user11, user12, user13);
        appendFriendRequest(user2, user9, user10, user11, user15, user16, user17);

        userRepository.saveAll(List.of(user1, user2, user3, user4,user5,user6,user7,user8, user9, user10, user11, user12, user13, user14, user15, user16, user17));

    }

    private void appendFriends(User user, User ...friends) {
        for(User friend : friends) {
            user.appendFriend(friend);
        }
    }

    private void appendFriendRequest(User toUser, User ...fromUsers) {
        Set<UserFriendRequest> receivedRequests = new HashSet<>(userFriendRequestRepository.findReceivedFriendRequestsByUserId(toUser.getId()));
        toUser.setReceivedFriendRequests(receivedRequests);
        for(User fromUser : fromUsers) {
            Set<UserFriendRequest> sentRequests = new HashSet<>(userFriendRequestRepository.findSentFriendRequestsByUserId(fromUser.getId()));
            fromUser.setSentFriendRequests(sentRequests);
            UserFriendRequest friendRequest = new UserFriendRequest(fromUser, toUser, UserFriendRequestStatus.WAITING_FOR_RESPONSE, Instant.now());
            userFriendRequestRepository.save(friendRequest);
            toUser.getReceivedFriendRequests().add(friendRequest);
            fromUser.getSentFriendRequests().add(friendRequest);
        }
    }

    private User findUserWithFriendsById(Long id) {
        User user = userRepository.findById(id).orElseThrow();
        Set<User> friendsField = userRepository.findFriendsField(id);
        Set<User> inverseFriendsField = userRepository.findInverseFriendsField(id);
        user.setFriends(friendsField);
        user.setInverseFriends(inverseFriendsField);
        return user;
    }


}
