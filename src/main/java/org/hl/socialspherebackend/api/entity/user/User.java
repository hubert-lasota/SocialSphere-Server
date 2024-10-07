package org.hl.socialspherebackend.api.entity.user;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "online", nullable = false)
    private boolean online;

    @Column(name ="created_at", nullable = false, columnDefinition = "datetime2")
    private Instant createdAt;

    @Column(name ="updated_at", nullable = false, columnDefinition = "datetime2")
    private Instant updatedAt;

    @Column(name ="last_online_at", columnDefinition = "datetime2")
    private Instant lastOnlineAt;

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile userProfile;

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfileConfig userProfileConfig;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Authority> authorities = new HashSet<>();

    @OneToMany(mappedBy = "sender", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserFriendRequest> sentFriendRequests = new HashSet<>();

    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserFriendRequest> receivedFriendRequests = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_friend_list",
            joinColumns = @JoinColumn(name = "friend_id"),
            inverseJoinColumns = @JoinColumn(name = "inverse_friend_id")
    )
    private Set<User> friends = new HashSet<>();

    @ManyToMany(mappedBy = "friends", fetch = FetchType.LAZY)
    private Set<User> inverseFriends = new HashSet<>();


    protected User() {

    }

    public User(String username, String password, Instant createdAt) {
        this(username, password, createdAt, null);
    }

    public User(String username, String password, Instant createdAt, Authority... authorities) {
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        online = false;
        if(authorities != null) {
            this.authorities.addAll(Set.of(authorities));
        }

    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void appendAuthority(Authority authority) {
        authorities.add(authority);
    }

    public void appendFriend(User friend) {
        this.friends.add(friend);
        friend.getInverseFriends().add(this);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserProfile getUserProfile() {
        return userProfile;
    }

    public void setUserProfile(UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public UserProfileConfig getUserProfileConfig() {
        return userProfileConfig;
    }

    public void setUserProfileConfig(UserProfileConfig userProfileConfig) {
        this.userProfileConfig = userProfileConfig;
    }

    public void setFriends(Set<User> friends) {
        this.friends = friends;
    }

    public void setInverseFriends(Set<User> inverseFriends) {
        this.inverseFriends = inverseFriends;
    }

    public Set<User> getFriends() {
        return friends;
    }

    public Set<User> getInverseFriends() {
        return inverseFriends;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public void setLastOnlineAt(Instant lastOnlineAt) {
        this.lastOnlineAt = lastOnlineAt;
    }

    public Instant getLastOnlineAt() {
        return lastOnlineAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setReceivedFriendRequests(Set<UserFriendRequest> receivedFriendRequests) {
        this.receivedFriendRequests = receivedFriendRequests;
    }

    public void setSentFriendRequests(Set<UserFriendRequest> sentFriendRequests) {
        this.sentFriendRequests = sentFriendRequests;
    }

    public Set<UserFriendRequest> getSentFriendRequests() {
        return sentFriendRequests;
    }

    public Set<UserFriendRequest> getReceivedFriendRequests() {
        return receivedFriendRequests;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", online=" + online +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", lastOnlineAt=" + lastOnlineAt +
                '}';
    }
}
