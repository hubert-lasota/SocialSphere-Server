package org.hl.socialspherebackend.api.entity.user;

import jakarta.persistence.*;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile userProfile;

    @OneToOne(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfileConfig userProfileConfig;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Authority> authorities = new HashSet<>();

    @OneToMany(mappedBy = "sender", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserFriendRequest> sentFriendRequests = new HashSet<>();

    @OneToMany(mappedBy = "receiver", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserFriendRequest> receivedFriendRequests = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Post> posts = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "user_friend_list",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "user_friend_id")
    )
    private Set<User> userFriendList = new HashSet<>();


    protected User() {

    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, Authority... authorities) {
        this.username = username;
        this.password = password;
        this.authorities.addAll(Set.of(authorities));
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

    public void appendSentFriendRequest(UserFriendRequest friendRequest) {
        this.sentFriendRequests.add(friendRequest);
    }

    public void appendReceivedFriendRequest(UserFriendRequest friendRequest) {
        this.receivedFriendRequests.add(friendRequest);
    }

    public void appendPost(Post post) {
        this.posts.add(post);
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


    public void setUserFriendList(Set<User> userFriendList) {
        this.userFriendList = userFriendList;
    }

    public Set<UserFriendRequest> getSentFriendRequests() {
        return sentFriendRequests;
    }

    public Set<UserFriendRequest> getReceivedFriendRequests() {
        return receivedFriendRequests;
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

    public Set<Post> getPosts() {
        return posts;
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
                '}';
    }

}
