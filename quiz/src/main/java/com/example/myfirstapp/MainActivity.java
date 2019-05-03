package com.example.myfirstapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String KEY_INDEX = "index";
    private static final String KEY_ANSWERED = "answered";
    private static final String KEY_CHEATED = "cheater";


    private static final int REQUEST_CODE_CHEAT = 0;

    private Button mTrueButton;
    private Button mFalseButton;
    private Button mCheatButton;
    private ImageButton mNextButton;
    private ImageButton mPrevButton;
    private TextView mQuestionTextView;
    private TextView mCheatsLeftTextView;

    private int correctAnswers;
    private int falseAnswers;
    private int mCheatsLeft = 3;
    private boolean mIsCheater;

    private boolean[] notAnswered;
    private boolean[] mCheatedQuestions;

    private Question[] mQuestionBank = new Question[]{
            new Question(R.string.question_australia, true),
            new Question(R.string.question_oceans, true),
            new Question(R.string.question_mideast, false),
            new Question(R.string.question_africa, false),
            new Question(R.string.question_americas, true),
            new Question(R.string.question_asia, true),
    };

    private int mCurrentIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "onCreate(Bundle) called");

        notAnswered = new boolean[mQuestionBank.length];
        mCheatedQuestions = new boolean[mQuestionBank.length];
        correctAnswers = 0;
        falseAnswers = 0;

        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(KEY_INDEX)) {
                mCurrentIndex = savedInstanceState.getInt(KEY_INDEX, 0);
            }

            if (savedInstanceState.containsKey(KEY_ANSWERED)) {
                notAnswered = savedInstanceState.getBooleanArray(KEY_ANSWERED);
            }

            if (savedInstanceState.containsKey(KEY_CHEATED)) {
                mCheatedQuestions = savedInstanceState.getBooleanArray(KEY_CHEATED);
            }

        }

        mQuestionTextView = (TextView) findViewById(R.id.question_text_view);
        mQuestionTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                mIsCheater = false;
                updateQuestion();
            }
        });

        mTrueButton = (Button) findViewById(R.id.true_button);
        mTrueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(true);

            }
        });

        mFalseButton = (Button) findViewById(R.id.false_button);
        mFalseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAnswer(false);
            }

        });

        mNextButton = (ImageButton) findViewById(R.id.next_button);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex + 1) % mQuestionBank.length;
                mIsCheater = false;
                updateQuestion();
            }
        });

        mPrevButton = (ImageButton) findViewById(R.id.prev_button);
        mPrevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentIndex = (mCurrentIndex - 1) % mQuestionBank.length;
                if (mCurrentIndex == -1) {
                    mCurrentIndex = mQuestionBank.length - 1;
                }
                updateQuestion();
            }
        });

        mCheatButton = (Button) findViewById(R.id.cheat_button);
        mCheatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCheatsLeft > 0) {
                    boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();
                    Intent intent = CheatActivity.newIntent(MainActivity.this, answerIsTrue);

                    startActivityForResult(intent, REQUEST_CODE_CHEAT);
                } else {
                    mCheatButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        mCheatsLeftTextView = (TextView) findViewById(R.id.cheats_left_text_view);
        mCheatsLeftTextView.setText(getString(R.string.number_of_cheats, mCheatsLeft));

        updateQuestion();
    }


    private void updateQuestion() {
        int question = mQuestionBank[mCurrentIndex].getTextResId();
        mQuestionTextView.setText(question);

        mTrueButton.setEnabled(!notAnswered[mCurrentIndex]);
        mFalseButton.setEnabled(!notAnswered[mCurrentIndex]);
        if (mCheatsLeft == 0) {
            mCheatButton.setVisibility(View.INVISIBLE);
        }
    }

    private void checkAnswer(boolean userPressedTrue) {
        boolean answerIsTrue = mQuestionBank[mCurrentIndex].isAnswerTrue();

        int messageResId = 0;
        if (mCheatedQuestions[mCurrentIndex]) {
            messageResId = R.string.judgment_toast;
            falseAnswers++;
        } else {
            if (userPressedTrue == answerIsTrue) {
                correctAnswers += 1;
                messageResId = R.string.correct_toast;
            } else {
                falseAnswers += 1;
                messageResId = R.string.incorrect_toast;
            }
        }

        notAnswered[mCurrentIndex] = true;
        mFalseButton.setEnabled(false);
        mTrueButton.setEnabled(false);


        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT)
                .show();

        if ((correctAnswers + falseAnswers) == mQuestionBank.length) {
            double correctPercentage = ((correctAnswers * 1.0) / (mQuestionBank.length)) * 100;
            String percentage = String.format("%.2f", correctPercentage);
            //percentage+=R.string.percentage;
            Toast toast = Toast.makeText(this, percentage+"% correct", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP, 0, 0);
            toast.show();

        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_CHEAT) {
            if (data == null) {
                return;
            }
        }

        mIsCheater = CheatActivity.wasAnswerShown(data);
        mCheatedQuestions[mCurrentIndex] = mIsCheater;
        mCheatsLeft--;
        mCheatsLeftTextView.setText(getString(R.string.number_of_cheats, mCheatsLeft));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        Log.i(TAG, "onSaveInstanceState");
        savedInstanceState.putInt(KEY_INDEX, mCurrentIndex);
        savedInstanceState.putBooleanArray(KEY_ANSWERED, notAnswered);
        savedInstanceState.putBooleanArray(KEY_CHEATED, mCheatedQuestions);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart() called");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume() called");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause() called");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop() called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");
    }

}
