package com.fileshareappv1.myapp.service;

import com.fileshareappv1.myapp.domain.Post;
import com.fileshareappv1.myapp.domain.User;
import com.fileshareappv1.myapp.domain.enumeration.Privacy;
import com.fileshareappv1.myapp.repository.PostRepository;
import com.fileshareappv1.myapp.repository.UserRepository;
import com.fileshareappv1.myapp.service.storage.StorageRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Profile("faker")
public class FakeDataService {

    private static final Logger LOG = LoggerFactory.getLogger(FakeDataService.class);

    @Value("${app.storage.location:/data/uploads}")
    private String uploadsLocation;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final StorageRepository storageRepository;

    public FakeDataService(PostRepository postRepository, UserRepository userRepository, StorageRepository storageRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.storageRepository = storageRepository;
    }

    @PostConstruct
    @Transactional
    public void generateFakeData() {
        LOG.info("Starting fake data generation with file scanning...");

        // Skip if posts already exist
        if (postRepository.count() > 0) {
            LOG.info("Posts already exist, skipping fake data generation");
            return;
        }

        try {
            // 1. Scan uploads directory for files
            List<String> availableFiles = scanUploadsDirectory();
            LOG.info("Found {} files in uploads directory", availableFiles.size());

            if (availableFiles.isEmpty()) {
                LOG.warn("No files found in uploads directory: {}", uploadsLocation);
                return;
            }

            // 2. Get admin user (or create if not exists)
            User adminUser = getOrCreateAdminUser();

            // 3. Generate fake posts
            generateFakePosts(adminUser, availableFiles);

            LOG.info("Fake data generation completed successfully");
        } catch (Exception e) {
            LOG.error("Error generating fake data", e);
        }
    }

    private List<String> scanUploadsDirectory() {
        Path uploadsDir = Path.of(uploadsLocation);

        if (!Files.exists(uploadsDir) || !Files.isDirectory(uploadsDir)) {
            LOG.warn("Uploads directory does not exist: {}", uploadsLocation);
            return Collections.emptyList();
        }

        try {
            return Files.list(uploadsDir)
                .filter(Files::isRegularFile)
                .map(Path::getFileName)
                .map(Path::toString)
                .filter(name -> {
                    String lower = name.toLowerCase();
                    return (
                        lower.endsWith(".png") ||
                        lower.endsWith(".jpg") ||
                        lower.endsWith(".jpeg") ||
                        lower.endsWith(".pdf") ||
                        lower.endsWith(".doc") ||
                        lower.endsWith(".docx") ||
                        lower.endsWith(".txt") ||
                        lower.endsWith(".gif")
                    );
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Error scanning uploads directory: {}", uploadsLocation, e);
            return Collections.emptyList();
        }
    }

    private User getOrCreateAdminUser() {
        return userRepository
            .findOneByLogin("admin")
            .orElseGet(() -> {
                LOG.warn("Admin user not found, using first available user");
                return userRepository.findAll().stream().findFirst().orElse(null);
            });
    }

    private void generateFakePosts(User adminUser, List<String> availableFiles) {
        List<FakePostData> fakePostsData = createFakePostsData();

        // Shuffle files for random distribution
        Collections.shuffle(availableFiles);

        int fileIndex = 0;

        for (int i = 0; i < fakePostsData.size(); i++) {
            FakePostData postData = fakePostsData.get(i);

            Post post = new Post();
            post.setContent(postData.content);
            post.setCreatedAt(Instant.now().minus(i + 1, ChronoUnit.DAYS));
            post.setUpdatedAt(post.getCreatedAt());
            post.setLocationName(postData.locationName);
            post.setLocationLat(postData.locationLat);
            post.setLocationLong(postData.locationLong);
            post.setPrivacy(postData.privacy);
            post.setViewCount(postData.viewCount);
            post.setCommentCount(postData.commentCount);
            post.setShareCount(postData.shareCount);
            post.setReactionCount(postData.reactionCount);
            post.setUser(adminUser);

            // Assign files to post (1-3 files per post)
            int numFilesForPost = Math.min(postData.numFiles, availableFiles.size() - fileIndex);
            List<String> postFiles = new ArrayList<>();

            for (int j = 0; j < numFilesForPost && fileIndex < availableFiles.size(); j++) {
                postFiles.add(availableFiles.get(fileIndex++));
            }

            post.setFiles(postFiles);
            post.setNumFiles(postFiles.size());

            postRepository.save(post);
            LOG.info("Created post {} with {} files: {}", i + 1, postFiles.size(), postFiles);
        }
    }

    private List<FakePostData> createFakePostsData() {
        List<FakePostData> posts = new ArrayList<>();

        posts.add(
            new FakePostData(
                "Chuyến đi Đà Lạt cuối tuần vừa rồi thật tuyệt vời! Thời tiết mát mẻ, cảnh đẹp như tranh vẽ. Cà phê ở đây thơm ngon và view nhìn ra thành phố từ trên cao thật là romantic.",
                "Da Lat, Lam Dong",
                new BigDecimal("11.94"),
                new BigDecimal("108.44"),
                Privacy.PUBLIC,
                245L,
                23L,
                5L,
                67L,
                3
            )
        );

        posts.add(
            new FakePostData(
                "Hôm nay mình học được một trick mới trong React.js về optimization performance. Sử dụng useMemo và useCallback đúng cách có thể cải thiện tốc độ render đáng kể!",
                "Unknown",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Privacy.PUBLIC,
                156L,
                34L,
                12L,
                45L,
                2
            )
        );

        posts.add(
            new FakePostData(
                "Món phở bò tái nóng hổi này làm mình nhớ quê hương quá! Nước dùng trong vắt, thịt bò tươi ngon, rau thơm đầy đủ. Không có gì bằng một bát phở nóng trong ngày lạnh.",
                "Ha Noi",
                new BigDecimal("21.0285"),
                new BigDecimal("105.8542"),
                Privacy.FRIENDS,
                89L,
                15L,
                3L,
                28L,
                2
            )
        );

        posts.add(
            new FakePostData(
                "Biển Nha Trang trong xanh, sóng vỗ êm ái. Được tắm biển và thưởng thức hải sản tươi sống ngay bên bờ biển. Cuộc sống này thật tuyệt vời!",
                "Nha Trang, Khanh Hoa",
                new BigDecimal("12.2388"),
                new BigDecimal("109.1967"),
                Privacy.PUBLIC,
                320L,
                45L,
                8L,
                102L,
                2
            )
        );

        posts.add(
            new FakePostData(
                "Tài liệu về Spring Boot Security mới nhất đây! Bao gồm JWT authentication, role-based authorization và best practices. Chia sẻ cho các bạn dev cùng học hỏi.",
                "Unknown",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Privacy.PUBLIC,
                78L,
                12L,
                25L,
                34L,
                2
            )
        );

        posts.add(
            new FakePostData(
                "Cafe nhỏ bên Hồ Gươm này có không khí cực kì thơ mộng. Ngồi đây coding và nhâm nhi cafe, nhìn người qua lại, cảm giác bình yên quá!",
                "Cafe Ho Guom, Ha Noi",
                new BigDecimal("21.0285"),
                new BigDecimal("105.8542"),
                Privacy.FRIENDS,
                45L,
                8L,
                2L,
                19L,
                1
            )
        );

        posts.add(
            new FakePostData(
                "Project File Share App đang dần hoàn thiện! Frontend React đã responsive tốt, backend Spring Boot xử lý upload/download file mượt mà. Sắp deploy lên production rồi!",
                "Unknown",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Privacy.PUBLIC,
                156L,
                23L,
                15L,
                56L,
                3
            )
        );

        posts.add(
            new FakePostData(
                "Sapa buổi sáng sớm trong sương mù trắng xóa. Leo lên đỉnh Fansipan để ngắm bình minh, cảm giác như đang ở trên thiên đường vậy!",
                "Sapa, Lao Cai",
                new BigDecimal("22.3364"),
                new BigDecimal("103.8438"),
                Privacy.PUBLIC,
                234L,
                31L,
                7L,
                78L,
                3
            )
        );

        posts.add(
            new FakePostData(
                "Phố cổ Hội An về đêm với những chiếc đèn lồng rực rỡ sắc màu. Đi thuyền trên sông Hoài, thả đèn hoa đăng và cầu nguyện điều tốt đẹp. Thật là lãng mạn!",
                "Hoi An, Quang Nam",
                new BigDecimal("15.8801"),
                new BigDecimal("108.3380"),
                Privacy.PUBLIC,
                189L,
                27L,
                6L,
                45L,
                2
            )
        );

        posts.add(
            new FakePostData(
                "Nghe nhạc acoustic chill chill trong lúc làm việc giúp tăng concentration đáng kể. Recommend mọi người playlist này cho những buổi coding marathon!",
                "Unknown",
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                Privacy.FRIENDS,
                67L,
                9L,
                4L,
                23L,
                1
            )
        );

        return posts;
    }

    private static class FakePostData {

        String content;
        String locationName;
        BigDecimal locationLat;
        BigDecimal locationLong;
        Privacy privacy;
        Long viewCount;
        Long commentCount;
        Long shareCount;
        Long reactionCount;
        int numFiles;

        FakePostData(
            String content,
            String locationName,
            BigDecimal locationLat,
            BigDecimal locationLong,
            Privacy privacy,
            Long viewCount,
            Long commentCount,
            Long shareCount,
            Long reactionCount,
            int numFiles
        ) {
            this.content = content;
            this.locationName = locationName;
            this.locationLat = locationLat;
            this.locationLong = locationLong;
            this.privacy = privacy;
            this.viewCount = viewCount;
            this.commentCount = commentCount;
            this.shareCount = shareCount;
            this.reactionCount = reactionCount;
            this.numFiles = numFiles;
        }
    }
}
