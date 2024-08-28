package org.hl.socialspherebackend.infrastructure.post;

import com.github.javafaker.Faker;
import org.hl.socialspherebackend.api.entity.post.Post;
import org.hl.socialspherebackend.api.entity.post.PostImage;
import org.hl.socialspherebackend.api.entity.user.User;
import org.hl.socialspherebackend.application.util.FileUtils;
import org.hl.socialspherebackend.infrastructure.user.UserRepository;
import org.springframework.beans.factory.InitializingBean;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class PostInitData implements InitializingBean {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostInitData(PostRepository postRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if(postRepository.count() > 0) {
            return;
        }
        createPosts();
    }

    public void createPosts() {
        Faker faker = new Faker();
        List<User> users = userRepository.findAll();
        users.forEach(u -> {
            for(int i = 0; i < 4; i++) {
                String content = faker.lorem().paragraph(7);
                Instant createdAt = faker.date().past(365, TimeUnit.DAYS).toInstant();
                Post post = new Post(content, 0L, 0L, createdAt, createdAt, u);
                int size = faker.random().nextInt(1, 4);
                Set<PostImage> postImages = new HashSet<>();

                for(int j = 0; j < size; j++) {
                    try {
                        PostImage img = new PostImage(
                                FileUtils.compressFile(generateRandomImage()), "image/png", "example.png", post);
                        postImages.add(img);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                if(!postImages.isEmpty()) {
                    post.setImages(postImages);
                }


                postRepository.save(post);
            }
        });
    }


    private byte[] generateRandomImage() throws IOException {
        int width = 300;
        int height = 300;
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
