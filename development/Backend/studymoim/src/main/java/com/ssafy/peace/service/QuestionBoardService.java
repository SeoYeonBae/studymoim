package com.ssafy.peace.service;

import com.ssafy.peace.dto.FreeBoardDto;
import com.ssafy.peace.dto.LectureDto;
import com.ssafy.peace.dto.QuestionBoardCommentDto;
import com.ssafy.peace.dto.QuestionBoardDto;
import com.ssafy.peace.entity.*;
import com.ssafy.peace.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.RollbackException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionBoardService {

    private final QuestionBoardRepository questionBoardRepository;
    private final QuestionBoardCommentRepository questionBoardCommentRepository;
    private final UserRepository userRepository;
    private final LectureRepository lectureRepository;
    private final AlarmRepository alarmRepository;

    @Transactional
    public Page<QuestionBoardDto.Detail> getQuestionBoardList(Pageable pageable) throws RollbackException {
        return questionBoardRepository.findAllByIsDeletedIsFalse(pageable)
                .map(questionBoard -> {
                    QuestionBoardDto.Detail res = QuestionBoardDto.Detail.fromEntity(questionBoard);
                    res.setQuestionBoardComments(
                            QuestionBoardDto.Detail.fromEntity(questionBoard).getQuestionBoardComments().stream()
                                    .filter(comment -> !comment.isDeleted())
                                    .collect(Collectors.toList()));
                    return res;
                });
    }

    @Transactional
    public Page<QuestionBoardDto.Detail> searchQuestionBoardByTitle(String word, Pageable pageable) throws RollbackException {
        return questionBoardRepository.findAllByIsDeletedIsFalseAndTitleContaining(word, pageable)
                .map(questionBoard -> {
                    QuestionBoardDto.Detail res = QuestionBoardDto.Detail.fromEntity(questionBoard);
                    res.setQuestionBoardComments(
                            QuestionBoardDto.Detail.fromEntity(questionBoard).getQuestionBoardComments().stream()
                                    .filter(comment -> !comment.isDeleted())
                                    .collect(Collectors.toList()));
                    return res;
                });
    }

    @Transactional
    public Page<QuestionBoardDto.Detail> searchQuestionBoardByContent(String word, Pageable pageable) throws RollbackException {
        return questionBoardRepository.findAllByIsDeletedIsFalseAndContentContaining(word, pageable)
                .map(questionBoard -> {
                    QuestionBoardDto.Detail res = QuestionBoardDto.Detail.fromEntity(questionBoard);
                    res.setQuestionBoardComments(
                            QuestionBoardDto.Detail.fromEntity(questionBoard).getQuestionBoardComments().stream()
                                    .filter(comment -> !comment.isDeleted())
                                    .collect(Collectors.toList()));
                    return res;
                });
    }

    @Transactional
    public QuestionBoardDto.Detail getQuestionBoardDetail(Integer articleId) throws RollbackException {
        questionBoardRepository.save(questionBoardRepository.findById(articleId).get().hit());
        QuestionBoard questionBoard = questionBoardRepository.findById(articleId).get();
        QuestionBoardDto.Detail res = QuestionBoardDto.Detail.fromEntity(questionBoard);
        res.setQuestionBoardComments(
                QuestionBoardDto.Detail.fromEntity(questionBoard).getQuestionBoardComments().stream()
                        .filter(comment -> !comment.isDeleted())
                        .collect(Collectors.toList()));
        return res;
    }

    @Transactional
    public QuestionBoardDto.Info writeQuestion(QuestionBoardDto.Write questionBoardDto) throws RollbackException {
        return QuestionBoardDto.Info.fromEntity(questionBoardRepository.save(QuestionBoard.builder()
                .title(questionBoardDto.getTitle())
                .content(questionBoardDto.getContent())
                .lecture(lectureRepository.findById(questionBoardDto.getLectureId()).get())
                .user(userRepository.findById(questionBoardDto.getUserId()).get())
                .build()));
    }

    @Transactional
    public QuestionBoardDto.Info updateQuestion(Integer questionBoardId, QuestionBoardDto.Write questionBoardDto) throws RollbackException {
        return QuestionBoardDto.Info.fromEntity(questionBoardRepository.save(
                questionBoardRepository.findById(questionBoardId).get()
                        .updateTitleAndContent(questionBoardDto.getTitle(), questionBoardDto.getContent())));
    }

    @Transactional
    public void deleteQuestion(Integer questionBoardId) throws RollbackException {
        questionBoardRepository.save(questionBoardRepository.findById(questionBoardId).get().delete());
    }

    @Transactional
    public QuestionBoardCommentDto.Info writeComment(QuestionBoardCommentDto.Write comment)
            throws RollbackException {
        QuestionBoardCommentDto.Info result = QuestionBoardCommentDto.Info.fromEntity(questionBoardCommentRepository.save(QuestionBoardComment.builder()
                .content(comment.getContent())
                .questionBoard(questionBoardRepository.findById(comment.getQuestionBoardId()).get())
                .user(userRepository.findById(comment.getUserId()).get())
                .build()));
        alarmRepository.save(Alarm.builder()
                .content(questionBoardRepository.findById(comment.getQuestionBoardId()).get().getTitle()+" 글에 댓글이 달렸습니다.")
                .user(userRepository.findById(comment.getUserId()).get())
                .user(userRepository.findById(questionBoardRepository.findById(comment.getQuestionBoardId()).get().getUser().getUserId()).get())
                .url("/community/question/" + comment.getQuestionBoardId())
                .build());
        return result;
    }

    @Transactional
    public QuestionBoardCommentDto.Info deleteComment(Integer commentId)
            throws RollbackException {
        QuestionBoardComment questionBoardComment = questionBoardCommentRepository.findById(commentId).get();
        return QuestionBoardCommentDto.Info.fromEntity(questionBoardCommentRepository.save(QuestionBoardComment.builder()
                        .questionBoard(questionBoardComment.getQuestionBoard())
                        .content(questionBoardComment.getContent())
                        .user(questionBoardComment.getUser())
                        .isDeleted(true)
                .build().updateId(commentId)));
    }

    @Transactional
    public List<QuestionBoardDto.Detail> getQuestionBoardListByLecture(Integer lectureId) throws RollbackException {
        return questionBoardRepository.findAllByIsDeletedIsFalseAndLecture_LectureId(lectureId).stream()
                .map(questionBoard -> {
                    QuestionBoardDto.Detail res = QuestionBoardDto.Detail.fromEntity(questionBoard);
                    res.setQuestionBoardComments(
                            QuestionBoardDto.Detail.fromEntity(questionBoard).getQuestionBoardComments().stream()
                                    .filter(comment -> !comment.isDeleted())
                                    .collect(Collectors.toList()));
                    return res;
                })
                .collect(Collectors.toList());
    }

    public Page<QuestionBoardDto.Info> getQuestionBoardListByCourse(Integer courseId, Pageable pageable) {
        return questionBoardRepository.findAllByIsDeletedIsFalseAndCourseId(courseId, pageable)
                .map(QuestionBoardDto.Info::fromEntity);
    }
}
