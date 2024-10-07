package org.hl.socialspherebackend.infrastructure.init_dev_data;

import com.github.javafaker.Faker;
import org.hl.socialspherebackend.api.entity.post.*;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.hl.socialspherebackend.infrastructure.post.PostCommentRepository;
import org.hl.socialspherebackend.infrastructure.post.PostRepository;
import org.hl.socialspherebackend.infrastructure.post.PostUpdateNotificationRepository;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;


class PostInitData {

    private final PostRepository postRepository;
    private final PostCommentRepository postCommentRepository;
    private final PostUpdateNotificationRepository postUpdateNotificationRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private int imgsIndex = 0;
    private List<byte[]> images;

    private int quotesIndex = 0;
    private final String[] quotes = {
            "Life is what happens when you're busy making other plans.",
            "The purpose of our lives is to be happy.",
            "Get busy living or get busy dying.",
            "You only live once, but if you do it right, once is enough.",
            "In the end, we only regret the chances we didn’t take.",
            "Live in the sunshine, swim in the sea, drink the wild air.",
            "The best way to predict your future is to create it.",
            "The journey of a thousand miles begins with one step.",
            "Don’t count the days, make the days count.",
            "Everything you can imagine is real.",
            "It is never too late to be what you might have been.",
            "You must be the change you wish to see in the world.",
            "It always seems impossible until it’s done.",
            "Turn your wounds into wisdom.",
            "What lies behind us and what lies before us are tiny matters compared to what lies within us.",
            "Happiness is not something ready-made. It comes from your own actions.",
            "The best revenge is massive success.",
            "Success is not how high you have climbed, but how you make a positive difference to the world.",
            "Dream big and dare to fail.",
            "Everything you’ve ever wanted is on the other side of fear.",
            "Hardships often prepare ordinary people for an extraordinary destiny.",
            "Happiness depends upon ourselves.",
            "Start where you are. Use what you have. Do what you can.",
            "Life is 10% what happens to us and 90% how we react to it.",
            "Do not go where the path may lead, go instead where there is no path and leave a trail.",
            "The only limit to our realization of tomorrow is our doubts of today.",
            "Believe you can and you're halfway there.",
            "Your time is limited, don’t waste it living someone else’s life.",
            "It does not matter how slowly you go as long as you do not stop.",
            "The harder you work for something, the greater you'll feel when you achieve it.",
            "Success usually comes to those who are too busy to be looking for it.",
            "Don't watch the clock; do what it does. Keep going.",
            "You don’t have to be great to start, but you have to start to be great.",
            "Success is not the key to happiness. Happiness is the key to success.",
            "The future belongs to those who believe in the beauty of their dreams.",
            "Believe in yourself and all that you are. Know that there is something inside you that is greater than any obstacle.",
            "Challenges are what make life interesting and overcoming them is what makes life meaningful.",
            "In the middle of every difficulty lies opportunity.",
            "Don’t wait. The time will never be just right.",
            "Act as if what you do makes a difference. It does.",
            "Keep your face always toward the sunshine—and shadows will fall behind you.",
            "I find that the harder I work, the more luck I seem to have.",
            "Don’t be afraid to give up the good to go for the great.",
            "The only place where success comes before work is in the dictionary.",
            "Opportunities don't happen. You create them.",
            "Success isn’t just about what you accomplish in your life; it’s about what you inspire others to do.",
            "The only way to do great work is to love what you do.",
            "If you can dream it, you can do it.",
            "Success is not in what you have, but who you are.",
            "Difficulties in life are intended to make us better, not bitter.",
            "Failure is another stepping stone to greatness.",
            "A goal is not always meant to be reached; it often serves simply as something to aim at.",
            "If opportunity doesn’t knock, build a door.",
            "Only those who dare to fail greatly can ever achieve greatly.",
            "To succeed in life, you need two things: ignorance and confidence.",
            "Success is walking from failure to failure with no loss of enthusiasm.",
            "Fall seven times, stand up eight.",
            "It’s not whether you get knocked down, it’s whether you get up.",
            "If you want to achieve greatness stop asking for permission.",
            "Success is not final, failure is not fatal: It is the courage to continue that counts.",
            "Great things never come from comfort zones.",
            "Success is what happens after you have survived all of your disappointments.",
            "Good things come to those who wait, but better things come to those who go out and get them.",
            "Success is the sum of small efforts, repeated day in and day out.",
            "The best time to plant a tree was 20 years ago. The second best time is now.",
            "Don’t stop when you’re tired. Stop when you’re done.",
            "We may encounter many defeats but we must not be defeated.",
            "Dream as if you’ll live forever, live as if you’ll die today.",
            "It’s not the years in your life that count, it’s the life in your years.",
            "Don’t count the days, make the days count.",
            "There is only one way to avoid criticism: do nothing, say nothing, and be nothing.",
            "Success seems to be connected with action. Successful people keep moving.",
            "I attribute my success to this: I never gave or took any excuse.",
            "You miss 100% of the shots you don’t take.",
            "Don’t be pushed around by the fears in your mind. Be led by the dreams in your heart.",
            "Don’t limit your challenges, challenge your limits.",
            "The key to success is to focus on goals, not obstacles.",
            "It’s not about how hard you hit. It’s about how hard you can get hit and keep moving forward.",
            "The road to success and the road to failure are almost exactly the same.",
            "If you really look closely, most overnight successes took a long time.",
            "Success is the result of preparation, hard work, and learning from failure.",
            "Life is either a daring adventure or nothing at all.",
            "The biggest risk is not taking any risk.",
            "Success is the ability to go from one failure to another with no loss of enthusiasm.",
            "You have to learn the rules of the game. And then you have to play better than anyone else.",
            "In order to be irreplaceable, one must always be different.",
            "The greater the obstacle, the more glory in overcoming it.",
            "You don’t have to be great to start, but you have to start to be great.",
            "Hardships often prepare ordinary people for an extraordinary destiny.",
            "The way to get started is to quit talking and begin doing.",
            "It always seems impossible until it’s done.",
            "If you want to make your dreams come true, the first thing you have to do is wake up.",
            "Don't wish it were easier. Wish you were better.",
            "What you do today can improve all your tomorrows.",
            "All our dreams can come true, if we have the courage to pursue them.",
            "The harder you work, the luckier you get.",
            "The secret of getting ahead is getting started.",
            "Success is where preparation and opportunity meet.",
            "The best dreams happen when you’re awake.",
            "Success is the sum of details.",
            "Do what you can, with what you have, where you are.",
            "It’s not the load that breaks you down, it’s the way you carry it.",
            "You get in life what you have the courage to ask for.",
            "Perseverance is not a long race; it is many short races one after the other.",
            "Failure is not falling down, but refusing to get up.",
            "Success is the progressive realization of a worthy goal.",
            "The successful warrior is the average man, with laser-like focus.",
            "Great works are performed not by strength but by perseverance.",
            "Strive not to be a success, but rather to be of value.",
            "It’s not about ideas. It’s about making ideas happen.",
            "Quality is not an act, it is a habit.",
            "The best way to succeed is to double your failure rate.",
            "Don't be afraid to give up the good to go for the great.",
            "You don’t learn to walk by following rules. You learn by doing and falling over.",
            "There are no traffic jams along the extra mile.",
            "Never let success get to your head and never let failure get to your heart.",
            "The difference between ordinary and extraordinary is that little extra.",
            "The road to success is dotted with many tempting parking spaces.",
            "A winner is a dreamer who never gives up.",
            "The secret to success is to know something nobody else knows.",
            "The expert in anything was once a beginner.",
            "Success doesn’t just find you. You have to go out and get it.",
            "It’s not what you look at that matters, it’s what you see.",
            "Great minds discuss ideas; average minds discuss events; small minds discuss people.",
            "It’s not about having the right opportunities. It’s about handling the opportunities right.",
            "Failure is success in progress.",
            "Success is falling nine times and getting up ten.",
            "If you don’t build your dream, someone else will hire you to help them build theirs.",
            "To be successful, you must accept all challenges that come your way. You can't just accept the ones you like.",
    };

    private int comIndex = 0;
    private final String[] comments = {
            "Great post! Really enjoyed reading it.",
            "I totally agree with what you're saying.",
            "Wow, this is amazing!",
            "Thanks for sharing this!",
            "This made my day, thank you!",
            "Incredible content, keep it up!",
            "I had no idea about this, thanks for the info!",
            "This is exactly what I needed today.",
            "Can't believe how awesome this is!",
            "So inspiring, love your work!",
            "You're absolutely right about this.",
            "This is such a helpful post, thanks!",
            "Fantastic read, learned a lot from this.",
            "Beautifully said! I couldn't agree more.",
            "This looks so interesting, can't wait to try it!",
            "This is awesome, keep doing what you're doing!",
            "Your content never disappoints, great job!",
            "Love this post, thanks for sharing!",
            "You always share the best insights.",
            "Such a well-written post, thank you!"
    };

    PostInitData(PostRepository postRepository, PostCommentRepository postCommentRepository, PostUpdateNotificationRepository postUpdateNotificationRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.postCommentRepository = postCommentRepository;
        this.postUpdateNotificationRepository = postUpdateNotificationRepository;
        this.userRepository = userRepository;
    }

    void initData() {
        if(postRepository.count() > 1) {
            return;
        }
        List<byte[]> fetchedImages = new ArrayList<>(50);
        for(int i = 0; i < 50; i++) {
            ResponseEntity<byte[]> fetchedEntity = restTemplate.getForEntity("https://picsum.photos/1200", byte[].class);
            fetchedImages.add(fetchedEntity.getBody());
        }
        images = fetchedImages;
        createPosts();
        addLikeAndComments();
    }

     private void createPosts() {
        Faker faker = new Faker();
        List<User> users = userRepository.findAll();
        users.forEach(u -> {
            for(int i = 0; i < 4; i++) {
                String content = getQuote();
                Instant createdAt = faker.date().past(365, TimeUnit.DAYS).toInstant();
                Post post = new Post(content, 0L, 0L, createdAt, createdAt, u);
                int size = faker.random().nextInt(1, 6);
                Set<PostImage> postImages = new HashSet<>();

                for(int j = 0; j < size; j++) {
                    PostImage img = new PostImage(FileUtils.compressFile(getImage()), "image/png", "example.png", post);
                    postImages.add(img);
                }

                if(!postImages.isEmpty()) {
                    post.setImages(postImages);
                }

                postRepository.save(post);
            }
        });
    }

    private void addLikeAndComments() {
        Pageable pageable = PageRequest.of(0, 10);
        User user1 = userRepository.findById(1L).orElseThrow();
        User user2 = userRepository.findById(2L).orElseThrow();
        Page<Post> user1Posts = postRepository.findByUser(pageable, user1);
        Page<Post> user2Posts = postRepository.findByUser(pageable, user2);

        List<User> user1Friends = userRepository.findUserFriends(user1.getId());
        List<User> user2Friends = userRepository.findUserFriends(user2.getId());

        for(User u : user1Friends) {
            for (Post p : user1Posts.getContent()) {
                Instant date = Instant.now();
                Set<PostComment> commentSet = postCommentRepository.findByPost(p);
                p.setComments(commentSet);
                PostComment com = new PostComment(u, p, getComment(), date, date);
                postCommentRepository.save(com);

                p.getComments().add(com);
                p.setCommentCount(p.getCommentCount() + 1);
                PostUpdateNotification comNotification = new PostUpdateNotification(p, u, PostUpdateType.COMMENT, date, false);
                postUpdateNotificationRepository.save(comNotification);
                Set<User> usersLikedPost = new HashSet<>(userRepository.findUsersLikedPost(p.getId()));
                p.setLikedBy(usersLikedPost);
                p.getLikedBy().add(u);
                p.setLikeCount(p.getLikeCount() + 1);
                PostUpdateNotification likeNotification = new PostUpdateNotification(p, u, PostUpdateType.LIKE, date, false);
                postUpdateNotificationRepository.save(likeNotification);

                postRepository.save(p);
            }
        }

        for(User u : user2Friends) {
            for(Post p : user2Posts.getContent()) {
                Instant now = Instant.now();
                Set<PostComment> commentSet = postCommentRepository.findByPost(p);
                p.setComments(commentSet);
                PostComment com = new PostComment(u, p, getComment(), now, now);
                postCommentRepository.save(com);

                p.getComments().add(com);
                p.setCommentCount(p.getCommentCount() + 1);
                PostUpdateNotification comNotification = new PostUpdateNotification(p, u, PostUpdateType.COMMENT, now, false);
                postUpdateNotificationRepository.save(comNotification);
                Set<User> usersLikedPost = new HashSet<>(userRepository.findUsersLikedPost(p.getId()));
                p.setLikedBy(usersLikedPost);
                p.getLikedBy().add(u);
                p.setLikeCount(p.getLikeCount() + 1);
                PostUpdateNotification likeNotification = new PostUpdateNotification(p, u, PostUpdateType.LIKE, now, false);
                postUpdateNotificationRepository.save(likeNotification);

                postRepository.save(p);
            }
        }

    }

    private String getComment() {
        if(comIndex >= comments.length) {
            comIndex = 0;
        }
        return comments[comIndex++];
    }

    private String getQuote() {
        if(quotesIndex >= quotes.length) {
            quotesIndex = 0;
        }
        return quotes[quotesIndex++];
    }

    private byte[] getImage() {
        if(imgsIndex >= images.size()) {
            imgsIndex = 0;
        }
        return images.get(imgsIndex++);
    }

}
