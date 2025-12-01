package ca.bcit.comp2522.lab11;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


/**
 * JavaFX quiz application that loads questions from a file,
 * asks 10 random questions, tracks score, and shows missed questions.
 *
 * @author Arshia Adamian
 * @author Rodrick Vyizigoro
 * @author Sukhraj Sandhar
 *
 * @version 1.0
 */
public class QuizApp extends Application
{
    private static final int QUESTIONS_PER_QUIZ = 10;
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 400;
    private static final int LINE_PARTS = 2;
    private static final int MOVE_TO_NEXT_QUESTION = 1;
    private static final int FIRST_PART = 0;
    private static final int SECOND_PART = 1;
    private static final int INITIAL_SCORE = 0;

    private final Map<String, String> questionsAndAnswers;
    private final Random random;
    private final List<String> quizQuestions;
    private final List<String> missedQuestions;


    private final Label questionLabel;
    private final Label scoreLabel;
    private final Label statusLabel;
    private final TextField answerField;
    private final Button startButton;
    private final Button submitButton;
    private final TextArea missedSummaryArea;

    private int currentQuestionIndex;
    private int score;

    /**
     * Constructor for the QuizApp.
     * Initializes the data structures and UI controls,
     * but does not build the scene yet.
     */
    public QuizApp()
    {
        questionsAndAnswers = new HashMap<>();
        random = new Random();
        quizQuestions = new ArrayList<>();
        missedQuestions = new ArrayList<>();
        questionLabel = new Label();
        scoreLabel = new Label();
        statusLabel = new Label();
        answerField = new TextField();
        startButton = new Button();
        submitButton = new Button();
        missedSummaryArea = new TextArea();
    }

    /**
     * Entry point for JavaFX applications.
     * Builds the user interface, and loads the quiz data.
     *
     * @param primaryStage the main window.
     */
    @Override
    public void start(final Stage primaryStage)
    {
        final VBox root;
        final Scene scene;

        questionLabel.setText("Press 'Start Quiz' to begin.");
        scoreLabel.setText("Score: " + INITIAL_SCORE + " / " + QUESTIONS_PER_QUIZ);
        statusLabel.setText("");

        answerField.setPromptText("Type your answer here");

        startButton.setText("Start Quiz");
        submitButton.setText("Submit");
        submitButton.setDisable(true);

        missedSummaryArea.setEditable(false);
        missedSummaryArea.setWrapText(true);
        missedSummaryArea.setPromptText("Missed questions will appear here after the quiz.");

        root = new VBox();
        root.getChildren().addAll(
            questionLabel,
            scoreLabel,
            statusLabel,
            answerField,
            submitButton,
            startButton,
            missedSummaryArea
                                 );

        root.getStyleClass().add("vbox");
        questionLabel.getStyleClass().add("label");
        scoreLabel.getStyleClass().add("label");
        statusLabel.getStyleClass().add("label");
        answerField.getStyleClass().add("text-field");
        submitButton.getStyleClass().add("button");
        startButton.getStyleClass().add("button");

        scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        final var cssUrl = getClass().getResource("styles.css");
        if (cssUrl != null)
        {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }


        startButton.setOnAction(event -> startQuiz());
        submitButton.setOnAction(event -> handleSubmit());

        answerField.setOnKeyPressed(event ->
                                    {
                                        if (event.getCode() == KeyCode.ENTER)
                                        {
                                            handleSubmit();
                                        }
                                    });

        primaryStage.setTitle("JavaFX Quiz - Test Time");
        primaryStage.setScene(scene);
        primaryStage.show();

        try
        {
            loadQuestionsFromFile();
        }
        catch (final IOException exception)
        {
            questionLabel.setText("Error loading quiz file.");
            statusLabel.setText("Check quiz.txt path.");
            submitButton.setDisable(true);
            startButton.setDisable(true);
        }
    }

    /**
     * Loads quiz questions and answers.
     * Each non-empty line is expected to be in the format:
     *     question|answer
     * The question becomes the key, and the answer becomes the value
     * in the questionsAndAnswers map.
     *
     * @throws IOException if there is an error opening or reading the file.
     */
    private void loadQuestionsFromFile()
        throws IOException
    {
        final Path path;
        path = Paths.get("quiz.txt");

        try (final BufferedReader reader = Files.newBufferedReader(path))
        {
            String line;

            while ((line = reader.readLine()) != null)
            {
                final String trimmedLine;
                final String[] parts;

                trimmedLine = line.trim();

                if (trimmedLine.isEmpty())
                {
                    continue;
                }

                parts = trimmedLine.split("\\|", LINE_PARTS);

                if (parts.length == LINE_PARTS)
                {
                    final String question;
                    final String answer;

                    question = parts[FIRST_PART].trim();
                    answer = parts[SECOND_PART].trim();

                    if (!question.isEmpty() && !answer.isEmpty())
                    {
                        questionsAndAnswers.put(question, answer);
                    }
                }
            }
        }
    }

    /**
     * Starts a new quiz session.
     * Resets score and state, clears previous results,
     * and randomly chooses QUESTIONS_PER_QUIZ questions
     * from the loaded questions.
     */
    private void startQuiz()
    {
        score = 0;
        currentQuestionIndex = 0;
        quizQuestions.clear();
        missedQuestions.clear();
        missedSummaryArea.clear();
        statusLabel.setText("");
        scoreLabel.setText("Score: 0 / " + QUESTIONS_PER_QUIZ);

        if (questionsAndAnswers.size() < QUESTIONS_PER_QUIZ)
        {
            questionLabel.setText("Not enough questions in quiz.txt.");
            submitButton.setDisable(true);
            return;
        }

        final List<String> allQuestions;
        int randomNumber;

        allQuestions = new ArrayList<>(questionsAndAnswers.keySet());


        for (int i = 0; i < QUESTIONS_PER_QUIZ; i++)
        {
            randomNumber = random.nextInt(allQuestions.size());
            quizQuestions.add(allQuestions.get(randomNumber));
        }

        // Prepare UI
        startButton.setDisable(true);
        submitButton.setDisable(false);
        answerField.setDisable(false);
        answerField.clear();

        showCurrentQuestion();
    }

    /**
     * Displays the current question on the screen.
     * If there are no more questions left, calls finishQuiz().
     */
    private void showCurrentQuestion()
    {
        if (currentQuestionIndex >= quizQuestions.size())
        {
            finishQuiz();
            return;
        }

        final String question;
        final int questionNumber;

        question = quizQuestions.get(currentQuestionIndex);
        questionNumber = currentQuestionIndex + MOVE_TO_NEXT_QUESTION;

        questionLabel.setText("Question " + questionNumber + "/" + QUESTIONS_PER_QUIZ + ": " + question);
        answerField.clear();
        answerField.requestFocus();
    }

    /**
     * Handles the user's answer when they click Submit or press ENTER.
     * Validates input, checks correctness, updates the score and missed
     * questions list, and then moves on to the next question.
     */
    private void handleSubmit()
    {
        if (currentQuestionIndex >= quizQuestions.size())
        {
            // Quiz already finished
            return;
        }

        final String userAnswerRaw;
        final String userAnswer;
        final String question;
        final String correctAnswer;

        userAnswerRaw = answerField.getText();
        userAnswer = userAnswerRaw.trim();

        if (userAnswer.isEmpty())
        {
            statusLabel.setText("Please enter an answer.");
            return;
        }

        question = quizQuestions.get(currentQuestionIndex);
        correctAnswer = questionsAndAnswers.get(question);

        if (userAnswer.equalsIgnoreCase(correctAnswer))
        {
            score++;
            statusLabel.setText("Correct!");
        }
        else
        {
            statusLabel.setText("Incorrect. Correct answer: " + correctAnswer);

            final String missedSummary;
            missedSummary = "Q: " + question + "\n" +
                "Correct: " + correctAnswer;

            missedQuestions.add(missedSummary);
        }

        scoreLabel.setText("Score: " + score + " / " + QUESTIONS_PER_QUIZ);

        currentQuestionIndex++;
        showCurrentQuestion();
    }

    /**
     * Called when all questions have been answered.
     * Shows the final score and a summary of all missed questions.
     * Re-enables the Start button so the user can play again.
     */
    private void finishQuiz()
    {
        submitButton.setDisable(true);
        startButton.setDisable(false);
        answerField.setDisable(true);

        final String finalMessage;
        finalMessage = "Quiz finished! Final score: " + score + " / " + QUESTIONS_PER_QUIZ;

        questionLabel.setText(finalMessage);

        if (missedQuestions.isEmpty())
        {
            missedSummaryArea.setText("Perfect! You did not miss any questions.");
        }
        else
        {
            final StringBuilder builder;
            builder = new StringBuilder();
            builder.append("Missed questions:")
                .append("\n\n");

            for (final String missed : missedQuestions)
            {
                builder.append(missed)
                    .append("\n\n");
            }

            missedSummaryArea.setText(builder.toString());
        }
    }

    /**
     * Main method that launches the JavaFX application.
     *
     * @param args command-line arguments (not used).
     */
    public static void main(final String[] args)
    {
        Application.launch(args);
    }
}
