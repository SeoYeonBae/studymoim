package com.ssafy.peace.service;

import com.ssafy.peace.dto.auth.UserRegisterPostReq;
import com.ssafy.peace.dto.*;
import com.ssafy.peace.entity.*;
import com.ssafy.peace.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final UserHistoryRepository userHistoryRepository;
    private final UserLikeCategoryRepository userLikeCategoryRepository;
    private final CourseCategoryRepository courseCategoryRepository;
    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final NoteRepository noteRepository;
    private final FreeBoardRepository freeBoardRepository;
    private final QuestionBoardRepository questionBoardRepository;
    private final FollowRepository followRepository;
    private final AlarmRepository alarmRepository;
    private final MessageRepository messageRepository;
    private final CourseTypeRepository courseTypeRepository;
    private final CourseRepository courseRepository;
    private final UserLikeCourseRepository userLikeCourseRepository;
    private final GCSService gcsService;

    public List<UserDto.Info> getUserList() throws RuntimeException {
        return null;
    }

    @Transactional
    public UserDto.Info createUser(UserRegisterPostReq userRegisterInfo) {
        userRepository.save(User.builder().email(userRegisterInfo.getEmail()).build());
        User user = userRepository.findByEmail(userRegisterInfo.getEmail());
        alarmRepository.save(Alarm.builder()
                .content("쓰임에 오신것을 환영합니다 :)")
                .user(user)
                .url("#")
                .build());
        return UserDto.Info.fromEntity(user);
//        User user = User.builder().email(userRegisterInfo.getEmail()).build();
//        return UserDto.Info.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserDto.Info updateUserInfo(MultipartFile file, UserDto.Start startInfo) throws RuntimeException, IOException {
        User user = userRepository.findById(startInfo.getUserId()).get();
        // 카테고리 초기화
        userLikeCategoryRepository.deleteAllByUser_userId(startInfo.getUserId());
        // 카테고리 새로 등록
        startInfo.getCategories().forEach(category -> userLikeCategoryRepository.save(UserLikeCategory.builder()
                .courseCategory(courseCategoryRepository.findById(category).get())
                .user(user)
                .build()));
        // 프로필 사진 등록
        if(file==null || file.isEmpty()){
            user.updateSaveName("logo.png");
        } else{
            gcsService.uploadProfileImage(file, user);
        }
        // 닉네임 등록
        user.updateNickname(startInfo.getNickname());
        return UserDto.Info.fromEntity(user);
    }

    @Transactional
    public UserDto.Info updateNickname(Integer userId, UserDto.Nickname nickname) {
        return UserDto.Info.fromEntity(userRepository.findById(userId).get().updateNickname(nickname.getNickname()));
    }

    @Transactional
    public UserDto.Info updateImage(Integer userId, MultipartFile file) throws IOException {
        User user = userRepository.findById(userId).get();
        if(file == null){
            user.updateSaveName("logo.png");
        } else{
            gcsService.uploadProfileImage(file, user);
        }
        return UserDto.Info.fromEntity(user);
    }

    public UserDto.Info getUserByEmail(String email) {
        // 디비에 유저 정보 조회 (userEmail을 통한 조회).
        User user = userRepository.findByEmail(email);
        if(user == null) return null;
        return UserDto.Info.fromEntity(userRepository.findByEmail(email));
    }

    public UserDto.Info getUserInfo(Integer userId) throws RuntimeException {
        return UserDto.Info.fromEntity(userRepository.findById(userId).get());
    }

    public List<StudyDto.Info> getStudyList(Integer userId) throws RuntimeException {
        return studyMemberRepository.findAllByUser_UserIdAndIsBannedIsFalse(userId).stream()
                .map(sm -> StudyDto.Info.fromEntity(sm.getStudy()))
                .collect(Collectors.toList());
    }

    public List<CourseDto.Info> getCourseHistory(Integer userId) {
        return userHistoryRepository.findAllByUser_userIdOrderByEndTimeDesc(userId).stream()
                .map(uh -> CourseDto.Info.fromEntity(uh.getLecture().getCourse()))
                .distinct()
                .collect(Collectors.toList());
    }

    public List<LectureDto.Info> getLectureHistory(Integer userId) throws RuntimeException {
        return userHistoryRepository.findAllByUser_userIdOrderByEndTimeDesc(userId).stream()
                .map(uh -> LectureDto.Info.fromEntity(uh.getLecture()))
                .distinct()
                .collect(Collectors.toList());
    }

    public List<NoteDto> getMemoList(Integer userId) {
        return noteRepository.findAllByUser_userId(userId).stream()
                .map(NoteDto::fromEntity)
                .collect(Collectors.toList());
    }

    public Map<String, Object> getPostList(Integer userId) {
        List<FreeBoardDto.Info> fList = freeBoardRepository.findAllByIsDeletedIsFalseAndUser_UserId(userId).stream()
                .map(FreeBoardDto.Info::fromEntity)
                .sorted(Comparator.comparing(FreeBoardDto.Info::getPublishTime).reversed())
                .collect(Collectors.toList());
        List<QuestionBoardDto.Info> qList = questionBoardRepository.findAllByIsDeletedIsFalseAndUser_UserId(userId).stream()
                .map(QuestionBoardDto.Info::fromEntity)
                .sorted(Comparator.comparing(QuestionBoardDto.Info::getPublishTime).reversed())
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("free", fList);
        result.put("question", qList);
        return result;
    }

    public List<CourseCategoryDto.Info> getCourseCategoryList(Integer userId) {
        return userLikeCategoryRepository.findAllByUser_userId(userId).stream()
                .map(userLikeCategory -> CourseCategoryDto.Info.fromEntity(userLikeCategory.getCourseCategory()))
                .collect(Collectors.toList());
    }

    public void setCourseCategoryList(Integer userId, List<CourseCategoryDto.Info> categoryList) {
        userLikeCategoryRepository.deleteAllByUser_userId(userId);
        userLikeCategoryRepository.saveAll(categoryList.stream().map(category -> UserLikeCategory.builder()
                .courseCategory(courseCategoryRepository.findById(category.getCourseCategoryId()).get())
                .user(userRepository.findById(userId).get())
                .build()).collect(Collectors.toList()));
    }

    public List<CourseDto.Info> getLikeList(Integer userId) {
        return userLikeCourseRepository.findAllByUser_userId(userId).get().stream()
                .map(userLikeCourse -> CourseDto.Info.fromEntity(userLikeCourse.getCourse()))
                .collect(Collectors.toList());
    }

    public long countFollowers(Integer userId) {
        return followRepository.countAllByToUser_UserId(userId);
    }

    public long countFollowings(Integer userId) {
        return followRepository.countAllByFromUser_UserId(userId);
    }

    public List<UserDto.Info> getFollowers(Integer userId) {
        return followRepository.findAllByToUser_UserId(userId).orElse(new ArrayList<>()).stream()
                .map(follow -> UserDto.Info.fromEntity(follow.getFromUser()))
                .collect(Collectors.toList());
    }

    public List<UserDto.Info> getFollowings(Integer userId) {
        return followRepository.findAllByFromUser_UserId(userId).orElse(new ArrayList<>()).stream()
                .map(follow -> UserDto.Info.fromEntity(follow.getToUser()))
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean followingStatus(Integer myUserId, Integer targetUserId) {
        if (followRepository.findByFromUser_UserIdAndToUser_UserId(myUserId, targetUserId).isPresent()) {
            return true;
        }
        return false;
    }

    @Transactional
    public UserDto.Info followUser(Integer myUserId, Integer targetUserId) {
        if (followRepository.findByFromUser_UserIdAndToUser_UserId(myUserId, targetUserId).isPresent()) {
            return null;
        }
        followRepository.save(Follow.builder()
                .fromUser(userRepository.findById(myUserId)
                        .orElseThrow(NullPointerException::new))
                .toUser(userRepository.findById(targetUserId)
                        .orElseThrow(NullPointerException::new)).build());
        alarmRepository.save(Alarm.builder()
                .content(userRepository.findById(myUserId).get().getNickname()+"님이 당신을 팔로우 하였습니다.")
                .user(userRepository.findById(targetUserId).get())
                .url("/mypage/"+myUserId)
                .build());
        return UserDto.Info.fromEntity(userRepository.findById(targetUserId).orElseThrow(NullPointerException::new));
    }

    @Transactional
    public UserDto.Info unfollowUser(Integer myUserId, Integer targetUserId) {
        Optional<Follow> resopt = followRepository.findByFromUser_UserIdAndToUser_UserId(myUserId, targetUserId);
        if (!resopt.isPresent()) {
            return null;
        }
        followRepository.deleteById(resopt.get().getFollowId());
        return UserDto.Info.fromEntity(userRepository.findById(targetUserId).orElseThrow(NullPointerException::new));
    }

    public boolean existUncheckdAlarm(Integer userId) {
        return alarmRepository.existsByUser_UserIdAndIsCheckedIsFalse(userId);
    }

    @Transactional
    public List<AlarmDto.Info> getAlarmList(Integer userId) {
        List<AlarmDto.Info> res = alarmRepository.findAllByUser_UserIdAndIsCheckedIsFalse(userId).stream()
                .map(AlarmDto.Info::fromEntity)
                .collect(Collectors.toList());
        // 읽지 않은 알림 리스트의 사이즈가 0이 아니라면 모든 알림 읽음 처리
        if(res.size() != 0){
            alarmRepository.checkAllByUser(userId);
        }
        return res;
    }

    public boolean existUncheckdMessage(Integer toUserId) {
        return messageRepository.existsByToUser_UserIdAndIsCheckedIsFalse(toUserId);
    }

    public List<UserDto.Info> getMessageUserList(Integer toUserId) {
        return messageRepository.findDistinctFromUser(toUserId).stream()
                                    .map(UserDto.Info::fromEntity)
                                    .collect(Collectors.toList());
    }

    @Transactional
    public List<MessageDto.Info> getMessageHistory(Integer toUserId, Integer fromUserId) {
        List<MessageDto.Info> list = messageRepository.findAllByToUser_UserIdAndFromUser_UserId(toUserId, fromUserId).stream()
                                        .map(MessageDto.Info::fromEntity)
                                        .collect(Collectors.toList());
        list.addAll(messageRepository.findAllByToUser_UserIdAndFromUser_UserId(fromUserId, toUserId).stream()
                .map(MessageDto.Info::fromEntity)
                .collect(Collectors.toList()));
        list.sort(new Comparator<MessageDto.Info>() {
            @Override
            public int compare(MessageDto.Info o1, MessageDto.Info o2) {
                return o1.getSendTime().compareTo(o2.getSendTime());
            }
        });
        // 메세지 기록 중 안 읽은 메세지 읽음 처리
        if(messageRepository.existsByToUser_UserIdAndFromUser_UserIdAndIsCheckedIsFalse(toUserId, fromUserId)){
            messageRepository.checkMessage(toUserId, fromUserId);
        }

        return list;
    }

    @Transactional
    public List<CourseDto.Info> getRecommendCourses(Integer userId) throws Exception{
        // 사용자가 좋아한 카테고리 리스트에서 아이디만 추출
        List<Integer> categoryIdList = userLikeCategoryRepository.findAllByUser_userId(userId).stream()
                .map(UserLikeCategoryDto.recommend::fromEntity)
                .collect(Collectors.toList());
        // 그 아이디가 달린 강좌 아이디 추출
        List<Integer> courseIdList = courseTypeRepository.findByCourseTypeIdIn(categoryIdList).stream()
                .map(CourseTypeDto.recommend::fromEntity)
                .collect(Collectors.toList());
        // 그 아이디로 강좌 리스트 추출
        return courseRepository.findByCourseIdIn(courseIdList).stream()
                .map(CourseDto.Info::fromEntity)
                .collect(Collectors.toList());
    }

}
