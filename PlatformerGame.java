import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.File;

import javafx.scene.input.MouseEvent;

import java.util.ArrayList;
import java.util.List;

public class PlatformerGame extends Application {

    private static double WINDOW_WIDTH = 1400;
    private static double WINDOW_HEIGHT = 800;
    private static final double GRAVITY = 0.6;
    private static final double JUMP_FORCE = 10;
    private static final double MOVEMENT_SPEED = 5;

    private boolean enemiesTrue = false;

    private int userBlocks = 10;
    private Text userBlocksText;

    private int currentLevel = 1;
    private Text levelText;

    private List<Enemy> enemies = new ArrayList<>();

    private Pane root;
    private Pane darkOverlay;
    private Button newGameButton;
    private ImageView player;
    private boolean jumping = false;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private double yVelocity = 0;
    private List<Node> platforms = new ArrayList<>();
    private ImageView portal;

    @Override
    public void start(Stage primaryStage) {
        // Получаем графический объект экрана
        Screen screen = Screen.getPrimary();
        // Получаем прямоугольник, представляющий размеры экрана
        Rectangle2D bounds = screen.getVisualBounds();

        // Устанавливаем размеры окна приложения равными размерам экрана
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());

        WINDOW_WIDTH = bounds.getWidth();
        WINDOW_HEIGHT = bounds.getHeight();

        root = new Pane();
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Artem, jump!");
        primaryStage.getIcons().add(new Image("assets/logo.png"));
        primaryStage.show();

        String musicFile = "assets/music.mp3";
        Media sound = new Media(new File(musicFile).toURI().toString());

        // Создание объекта MediaPlayer
        MediaPlayer mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.play();

        primaryStage.setOnCloseRequest(event -> mediaPlayer.stop());

        level1();
        createOverlay();

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.SPACE && !jumping) {
                jump();
            } else if (event.getCode() == KeyCode.A) {
                movingLeft = true;
            } else if (event.getCode() == KeyCode.D) {
                movingRight = true;
            } else if (event.getCode() == KeyCode.N) {
                goToNextLevel();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                darkOverlay.setVisible(true);
            }
        });

        scene.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.A) {
                movingLeft = false;
            } else if (event.getCode() == KeyCode.D) {
                movingRight = false;
            }
        });

        scene.setOnMousePressed(this::handleMousePressed);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                update();
                if (enemiesTrue == true) {
                    updateEnemies();
                }
            }
        };
        timer.start();
    }

    private void creteBgImage(String img) {
        Image backgroundImage = new Image(img); // Замените "assets/background.jpg" на путь к вашей картинке
        ImageView backgroundImageView = new ImageView(backgroundImage);
        backgroundImageView.setFitWidth(WINDOW_WIDTH); // Установите ширину изображения равной ширине окна
        backgroundImageView.setFitHeight(WINDOW_HEIGHT); // Установите высоту изображения равной высоте окна
        root.getChildren().add(backgroundImageView);
    }

    private void createPlayer() {
        Image playerImage = new Image("assets/player.png");
        player = new ImageView(playerImage);
        player.setFitWidth(100);
        player.setFitHeight(100);
        player.setTranslateX(100);
        player.setTranslateY(700);
        root.getChildren().add(player);
    }

    private void createPlatforms(String img) {
        // Пример создания стартовой платформы
        Image groundTexture = new Image(img);
        ImageView groundView = new ImageView(groundTexture);
        groundView.setFitWidth(WINDOW_WIDTH);
        groundView.setFitHeight(0);
        groundView.setTranslateY(WINDOW_HEIGHT - 90);
        root.getChildren().add(groundView);
        platforms.add(groundView);
    }

    private void createPortal(int x, int y) {
        Image portalImage = new Image("assets/portal.gif");
        portal = new ImageView(portalImage);
        portal.setFitWidth(100);
        portal.setFitHeight(100);
        portal.setTranslateX(x); // координаты портала
        portal.setTranslateY(y);
        root.getChildren().add(portal);
    }

    private void createLevelText(Color color) {
        levelText = new Text("Уровень: " + currentLevel);
        levelText.setFill(color);
        levelText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        levelText.setTranslateX(WINDOW_WIDTH - 150);
        levelText.setTranslateY(50);
        root.getChildren().add(levelText);
    }

    private void createBlocksText(Color color) {
        userBlocksText = new Text("Осталось блоков: " + userBlocks);
        userBlocksText.setFill(color);
        userBlocksText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        userBlocksText.setTranslateX(WINDOW_WIDTH - 260);
        userBlocksText.setTranslateY(100);
        root.getChildren().add(userBlocksText);
    }

    private void createEnemies() {
        Image enemyImage = new Image("assets/goomba.png");
        Enemy enemy1 = new Enemy(enemyImage, 10); // Создание врага с изображением и скоростью
        Enemy enemy2 = new Enemy(enemyImage, 2); // Пример создания другого врага с разной скоростью
        // Установка начальных координат для врагов
        enemy1.setTranslateX(WINDOW_WIDTH);
        enemy1.setTranslateY(600);
        enemy1.setFitWidth(70);
        enemy1.setFitHeight(100);
        enemy2.setTranslateX(WINDOW_WIDTH);
        enemy2.setTranslateY(400);
        enemy2.setFitWidth(70);
        enemy2.setFitHeight(100);
        // Добавление врагов на экран
        root.getChildren().addAll(enemy1, enemy2);
        // Добавление врагов в список для дальнейшего обновления
        enemies.add(enemy1);
        enemies.add(enemy2);
    }

    private void updateBlocksText(Color color) {
        userBlocksText.setText("Осталось блоков: " + userBlocks);
    }

    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            enemy.move(); // Перемещение врагов влево
            // Если враг достигает левой границы экрана, возвращаем его вправо за пределы
            // экрана
            if (enemy.getTranslateX() <= 0) {
                enemy.setTranslateX(WINDOW_WIDTH);
            }
            // Проверка столкновения с игроком
            if (player.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                enemies.clear();
                level1();
            }
        }
    }

    private void createOverlay() {
        darkOverlay = new Pane();
        darkOverlay.setStyle("-fx-background-color: rgba(0,0,0,0.5);");
        darkOverlay.setVisible(false);

        darkOverlay.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

        root.getChildren().add(darkOverlay);

        newGameButton = new Button("Новая Игра");
        newGameButton.setStyle(
                "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 10px 20px; -fx-border-radius: 10px;");

        newGameButton.setLayoutX((WINDOW_WIDTH - 200) / 2); // Изменение расположения и размера кнопки
        newGameButton.setLayoutY((WINDOW_HEIGHT - 50) / 2);

        newGameButton.setOnAction(event -> level1());

        darkOverlay.getChildren().add(newGameButton);
    }

    private void handleMousePressed(MouseEvent event) {
        if (event.isSecondaryButtonDown()) {
            if (userBlocks > 0) {
                createPlatform(event.getX(), event.getY(), 80, 40);
                userBlocks--;
                updateBlocksText(Color.BLACK);
            }
        }
    }

    private void createPlatform(double x, double y, double width, double height) {
        // Создаем прямоугольник для обработки коллизий
        Rectangle platformCollider = new Rectangle(30, 30);
        platformCollider.setFill(Color.TRANSPARENT); // Прозрачный цвет, чтобы коллайдер был невидимым
        platformCollider.setTranslateX(x + 30);
        platformCollider.setTranslateY(y);
        root.getChildren().add(platformCollider);
        platforms.add(platformCollider);

        // Затем добавляем изображение платформы с небольшим смещением
        Image platformTexture = new Image("assets/newPlatform.png");
        ImageView platformView = new ImageView(platformTexture);
        platformView.setFitWidth(width);
        platformView.setFitHeight(height);
        platformView.setTranslateX(x);
        platformView.setTranslateY(y);
        root.getChildren().add(platformView);
    }

    private void jump() {
        Image playerJumpImage = new Image("assets/player_jump.png");
        player.setImage(playerJumpImage);

        jumping = true;
        yVelocity = -JUMP_FORCE;
    }

    private void update() {
        // Обновляем положение персонажа по вертикали
        if (player.getTranslateY() < WINDOW_HEIGHT - player.getBoundsInLocal().getHeight()) {
            // Применяем гравитацию
            yVelocity += GRAVITY;
            player.setTranslateY(player.getTranslateY() + yVelocity);
        } else {
            // Персонаж касается земли
            jumping = false;
            yVelocity = 0;
            player.setTranslateY(WINDOW_HEIGHT - player.getBoundsInLocal().getHeight());
        }

        // Проверяем коллизию персонажа с платформами
        for (Node platform : platforms) {
            if (player.getBoundsInParent().intersects(platform.getBoundsInParent())) {
                // Проверяем, что персонаж находится над платформой и падает вниз
                if (player.getTranslateY() < platform.getTranslateY() && yVelocity >= 0) {
                    // Устанавливаем положение персонажа на уровне верхней границы платформы
                    player.setTranslateY(platform.getTranslateY() - player.getBoundsInLocal().getHeight());
                    // Сбрасываем скорость по вертикали
                    yVelocity = 0;
                    // Отключаем прыжок
                    jumping = false;

                    // Восстановление текстуры персонажа
                    Image playerImage = new Image("assets/player.png");
                    player.setImage(playerImage);
                } else if (player.getTranslateY() + player.getBoundsInLocal().getHeight() > platform.getTranslateY()
                        && yVelocity < 0) {
                    // Если персонаж касается платформы сверху, прекращаем его вертикальное движение
                    yVelocity = 0;
                    // Устанавливаем положение персонажа на уровне нижней границы платформы
                    player.setTranslateY(platform.getTranslateY() + platform.getBoundsInLocal().getHeight());
                }
            }
        }

        // Проверка, если игрок касается портала
        if (player.getBoundsInParent().intersects(portal.getBoundsInParent())) {
            // Переходим на новый уровень
            goToNextLevel();
        }

        // Движение влево и вправо
        if (movingLeft) {
            player.setTranslateX(player.getTranslateX() - MOVEMENT_SPEED);
            // Проверяем коллизию с левой границей окна
            if (player.getTranslateX() < 0) {
                player.setTranslateX(0);
            }
        }
        if (movingRight) {
            player.setTranslateX(player.getTranslateX() + MOVEMENT_SPEED);
            // Проверяем коллизию с правой границей окна
            if (player.getTranslateX() + player.getBoundsInLocal().getWidth() > WINDOW_WIDTH) {
                player.setTranslateX(WINDOW_WIDTH - player.getBoundsInLocal().getWidth());
            }
        }
    }

    private void goToNextLevel() {
        // Очищаем все элементы на экране
        root.getChildren().clear();
        platforms.clear();

        currentLevel++;

        if (currentLevel == 2) {
            level2();
        } else if (currentLevel == 3) {
            level3();
        } else if (currentLevel == 4) {
            level4();
        } else {
            endGame();
        }
    }

    private void level1() {
        currentLevel = 1;

        root.getChildren().clear(); // Очистка предыдущих объектов

        platforms.clear();

        userBlocks = 10;

        creteBgImage("assets/lvl1_bg.gif");
        createPlatforms("assets/floar.png");
        createPortal(50, 50); // Добавляем портал
        createPlayer();
        createLevelText(Color.BLACK);
        createBlocksText(Color.BLACK);
        createOverlay();
    }

    private void level2() {
        // Очищаем старые платформы
        platforms.clear();

        userBlocks = 7;

        creteBgImage("assets/lvl2_bg.gif");

        Image wallTexture = new Image("assets/wall2.png");
        ImageView wallView = new ImageView(wallTexture);
        wallView.setFitWidth(WINDOW_WIDTH);
        wallView.setFitHeight(50);
        wallView.setTranslateX(-150);
        wallView.setTranslateY(500);
        root.getChildren().add(wallView);
        platforms.add(wallView);

        createPlatforms("assets/floar2.png");
        createPortal(50, 200);
        createPlayer();
        createLevelText(Color.WHITE);
        createBlocksText(Color.WHITE);
        createOverlay();
    }

    private void level3() {
        // Очищаем старые платформы
        platforms.clear();

        userBlocks = 10;

        creteBgImage("assets/lvl3_bg.jpg");

        Image wallTexture = new Image("assets/wall2.png");
        ImageView wallView = new ImageView(wallTexture);
        wallView.setFitWidth(WINDOW_WIDTH);
        wallView.setFitHeight(50);
        wallView.setTranslateX(-(WINDOW_WIDTH / 1.3));
        wallView.setTranslateY(500);
        root.getChildren().add(wallView);
        platforms.add(wallView);

        ImageView wallView2 = new ImageView(wallTexture);
        wallView2.setFitWidth(100);
        wallView2.setFitHeight(500);
        wallView2.setTranslateX(400);
        wallView2.setTranslateY(50);
        root.getChildren().add(wallView2);
        platforms.add(wallView2);

        createPlatforms("assets/floar3.png");
        createPortal(50, 50);
        createPlayer();
        createLevelText(Color.WHITE);
        createBlocksText(Color.WHITE);
        createOverlay();
    }

    private void level4() {
        // Очищаем старые платформы
        platforms.clear();

        userBlocks = 8;

        creteBgImage("assets/lvl4_bg.gif");

        Image wallTexture = new Image("assets/wall4.png");
        ImageView wallView = new ImageView(wallTexture);
        wallView.setFitWidth(250);
        wallView.setFitHeight(60);
        wallView.setTranslateX((int) WINDOW_WIDTH / 2);
        wallView.setTranslateY((int) WINDOW_HEIGHT / 2);
        root.getChildren().add(wallView);
        platforms.add(wallView);

        createPlatforms("assets/floar4.png");
        createPortal((int) WINDOW_WIDTH / 2, (int) WINDOW_HEIGHT / 3);
        createPlayer();
        enemiesTrue = true;
        createEnemies();
        createLevelText(Color.WHITE);
        createBlocksText(Color.WHITE);
        createOverlay();
    }

    private void endGame() {
        // Очищаем старые платформы
        enemiesTrue = false;
        platforms.clear();
        creteBgImage("assets/end.png");
    }

    public static void main(String[] args) {
        launch(args);
    }

}
