package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.example.trivia.controller.AppController;
import com.example.trivia.data.AnswerListAsyncResponse;
import com.example.trivia.data.Repository;
import com.example.trivia.databinding.ActivityMainBinding;
import com.example.trivia.model.Question;
import com.example.trivia.model.Score;
import com.example.trivia.util.Preferences;
import com.google.android.material.snackbar.Snackbar;
//import com.example.trivia.controller.AppController;

import org.json.JSONArray;

import java.text.MessageFormat;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private int currentQuestionIndex = 0;
    private List<Question> questions;

    private int scoreCounter = 0;
    private Score score;

    private Preferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        score = new Score();
        prefs = new Preferences(MainActivity.this);



        binding.scoreText.setText(MessageFormat.format("The Current Score: {0}", String.valueOf(score.getScore())));
        binding.highestScore.setText(String.format("The Highest Score: %d", prefs.getHighestScore()));

        currentQuestionIndex = prefs.getState();

        questions =  new Repository().getQuestions(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(List<Question> questionArrayList) {
                binding.questionTextView.setText(questionArrayList.get(currentQuestionIndex).getAnswer());
                updateCounter(questionArrayList);
            }
        });

        binding.buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToNextQuestion();
                binding.highestScore.setText(String.format("The Highest Score: %d", prefs.getHighestScore()));
            }
        });

        binding.buttonTrue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(true);
                updateQuestions();
                binding.highestScore.setText(String.format("The Highest Score: %d", prefs.getHighestScore()));
            }
        });
        binding.buttonFalse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAnswer(false);
                updateQuestions();
                binding.highestScore.setText(String.format("The Highest Score: %d", prefs.getHighestScore()));
            }
        });
    }

    private void goToNextQuestion() {
        currentQuestionIndex = (currentQuestionIndex + 1) % questions.size();
        updateQuestions();
        prefs.saveHighestScore(score.getScore());
    }

    private void checkAnswer(boolean userChoice) {
        boolean answer = questions.get(currentQuestionIndex).isAnswerTrue();
        int snackMessageId;
        if(userChoice == answer) {
            snackMessageId = R.string.correctAnswer;
            fadeAnimation();
            addPoints();
        }
        else {
            snackMessageId = R.string.incorrectAnswer;
            shakeAnimation();
            deductPoints();
        }

        Snackbar.make(binding.cardView, snackMessageId,Snackbar.LENGTH_SHORT).show();
    }

    private void updateCounter(List<Question> questionArrayList) {
        binding.textViewOutOf.setText(String.format(getString(R.string.text_formatted), currentQuestionIndex, questionArrayList.size()));
    }

    private void updateQuestions() {
        String question = questions.get(currentQuestionIndex).getAnswer();
        binding.questionTextView.setText(question);
        updateCounter(questions);
    }

    private void addPoints() {
        scoreCounter += 100;
        score.setScore(scoreCounter);
        binding.scoreText.setText(MessageFormat.format("The Current Score: {0}", String.valueOf(score.getScore())));
    }

    private void deductPoints() {
        scoreCounter -= 100;
        if(scoreCounter < 0) scoreCounter = 0;
        score.setScore(scoreCounter);
        Log.d("Score", String.valueOf(score.getScore()));
        binding.scoreText.setText(MessageFormat.format("The Current Score: {0}", String.valueOf(score.getScore())));
    }

    private void fadeAnimation() {
        AlphaAnimation alphaAnimation = new AlphaAnimation(1.0f, 0.0f);
        alphaAnimation.setDuration(300);
        alphaAnimation.setRepeatCount(1);
        alphaAnimation.setRepeatMode(Animation.REVERSE);

        binding.cardView.setAnimation(alphaAnimation);

        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.questionTextView.setTextColor(Color.GREEN);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.questionTextView.setTextColor(Color.WHITE);
                goToNextQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void shakeAnimation() {
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_animation);
        binding.cardView.setAnimation(shake);

        shake.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.questionTextView.setTextColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                binding.questionTextView.setTextColor(Color.WHITE);
                goToNextQuestion();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    @Override
    protected void onPause() {
        prefs.setState(currentQuestionIndex);
        prefs.saveHighestScore(score.getScore());
        super.onPause();
    }
}