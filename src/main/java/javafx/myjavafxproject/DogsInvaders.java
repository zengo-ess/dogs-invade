package meowmeowmeow;



import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class DogsInvaders extends Application {
    private static final Random RAND = new Random();
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final int PLAYER_SIZE = 120;

    static final Image PLAYER_IMG = new Image(DogsInvaders.class.getResource("/Cute-Cat-icon.png").toExternalForm());
    static final Image EXPLOSION_IMG  = new Image(DogsInvaders.class.getResource("/explosion.png").toExternalForm());
    static final int EXPLOSION_W =128;
    static final int EXPLOSION_ROWS =3;
    static final int EXPLOSION_COL =3;
    static final int EXPLOSION_H =128;
    static final int EXPLOSION_STEPS =15;

    static final Image BOMBS_IMG[] = {
            new Image(DogsInvaders.class.getResource("/pngtree-angry-cute-dog-sticker-png-image_6562186.png").toExternalForm()),
            new Image(DogsInvaders.class.getResource("/pngtree-cute-dog-illustration-png-image_9009549.png").toExternalForm()),
            new Image(DogsInvaders.class.getResource("/pngtree-cute-dog-vector-png-image_6612077.png").toExternalForm())
    };

    final int MAX_BOMBS = 10, MAX_SHOTS = MAX_BOMBS * 2;
    boolean gameOver = false;
    private GraphicsContext gc;


    Cat player;
    List<Shot> shots;
    List<Universe> univ;
    List<Bomb> bombs;

    private double mouseX;
    private int score;





    @Override
    public void start(Stage stage) throws Exception {
        music();

        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.PINK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);




        Timeline timeline = new Timeline(new KeyFrame(Duration.millis(100), e -> run(gc)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
        canvas.setCursor(Cursor.MOVE);
        canvas.setOnMouseMoved(e -> mouseX = e.getX());
        canvas.setOnMouseClicked(e -> {
            if(shots.size() < MAX_SHOTS) shots.add(player.shoot());
            if(gameOver) {
                gameOver = false;
                setup();
            }
        });
        setup();
        stage.setScene(new Scene(new StackPane(canvas)));
        stage.setTitle("котовые войны");
        stage.show();

    }
    MediaPlayer mediaPlayer;
    public void music() {
        String s = getClass().getResource("/v12044gd0000cndbrffog65r9g0dvvk0.mp3").toExternalForm();
        Media h = new Media(Paths.get(s).toUri().toString());
        mediaPlayer = new MediaPlayer(h);
        mediaPlayer.play();
    }




    //настройка игры
    private void setup() {
        univ = new ArrayList<>();
        shots = new ArrayList<>();
        bombs = new ArrayList<>();
        player = new Cat(WIDTH/2, HEIGHT - PLAYER_SIZE, PLAYER_SIZE, PLAYER_IMG);
        score = 0;
        IntStream.range(0, MAX_BOMBS).mapToObj(i ->this.newBomb()).forEach(bombs::add);



    }


    private void run(GraphicsContext gc) {
        gc.setFill(Color.PINK);
        gc.fillRect(0, 0, WIDTH, HEIGHT);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font(20));
        gc.setFill(Color.BLACK);
        gc.fillText("Счет: " + score, 60,  20);



        if(gameOver) {
            gc.setFont(Font.font(35));
            gc.setFill(Color.RED);
            gc.fillText("Конец игры \n Ваш счет: " + score + " \n Нажми, чтобы играть снова", WIDTH / 2, HEIGHT /2.5);
            //	return;
        }

        univ.forEach(Universe::draw);

        player.update();
        player.draw();
        player.posX =(int)mouseX;
        bombs.stream().peek(Cat::update).peek(Cat::draw).forEach(e ->{
            if(player.collide(e) && !player.exploding) {
                player.explode();

            }
        });

        for(int i =shots.size() - 1; i >= 0; i--) {
            Shot shot = shots.get(i);
            if(shot.posY < 0 ||shot.toRemove) {
                shots.remove(i);
                continue;
            }
            shot.update();
            shot.draw();
            for(Bomb bomb : bombs) {
                if(shot.collide(bomb)&& !bomb.exploding) {
                    score++;
                    bomb.explode();
                    shot.toRemove = true;
                }
            }
        }
        for(int i =bombs.size() - 1; i >= 0;i--) {
            if(bombs.get(i).destroyed) {
                bombs.set(i, newBomb());
            }
        }
        gameOver = player.destroyed;
        if(RAND.nextInt(10)>2) {
            univ.add(new Universe());
        }
        for(int i =0;i < univ.size(); i++) {
            if(univ.get(i).posY> HEIGHT)
                univ.remove(i);
        }

    }


    public class Cat{
        int posX;
        int posY;
        int size;
        boolean exploding, destroyed;
        Image img;
        int explosionStep = 0;


        //конструктор
        public Cat(int posX, int posY, int size, Image image) {
            this.posX = posX;
            this.posY = posY;
            this.size = size;
            img = image;


        }

        public Shot shoot() {
            return new Shot(posX + size / 2 - Shot.size / 2, posY - Shot.size);
        }

        public void update() {
            if(exploding)explosionStep++;
            destroyed = explosionStep > EXPLOSION_STEPS;

        }
        public void draw() {
            if(exploding) {
                gc.drawImage(EXPLOSION_IMG, explosionStep % EXPLOSION_COL * EXPLOSION_W, (explosionStep/EXPLOSION_ROWS)* EXPLOSION_H + 1,
                        EXPLOSION_H, EXPLOSION_W, posX, posY, size,size);
            }
            else {
                gc.drawImage(img, posX, posY, size, size);
            }
        }


        public boolean collide(Cat other) {

            int d = distance(this.posX + size/2, this.posY + size /  2,
                    other.posX + other.size /2, other.posY + other.size /2);
            return d < other.size / 2 + this.size / 2;
        }
        public void explode() {
            exploding = true;
            explosionStep = -1;


        }


    }



    //компьютер

    public class Bomb extends Cat{
        int SPEED = (score/5)+2;

        public Bomb(int posX, int posY, int size, Image image) {
            super(posX, posY, size, image);
        }

        public void update() {
            super.update();
            if(!exploding && !destroyed) posY += SPEED;
            if(posY > HEIGHT) destroyed = true;
        }
    }

    //пули

    public class Shot {

        public boolean toRemove;

        int posX, posY, speed = 10;
        static final int size = 6;

        public Shot(int posX, int posY) {
            this.posX = posX;
            this.posY = posY;
        }

        public void update() {
            posY-=speed;
        }


        public void draw() {
            gc.setFill(Color.DEEPPINK);
            if (score >=50 && score<=70 || score>=120) {
                gc.setFill(Color.ORANGERED);
                speed = 50;
                gc.fillRect(posX-5, posY-10, size+10, size+30);
            } else {
                gc.fillOval(posX, posY, size, size);
            }
        }
        public boolean collide(Cat cat) {
            int distance = distance(this.posX + size / 2, this.posY + size / 2,
                    cat.posX + cat.size / 2, cat.posY + cat.size / 2);
            return distance  < cat.size / 2 + size / 2;
        }


    }

    //окр среда

    public class Universe{
        int posX, posY;
        private int h,w, r,g ,b;
        private double opacity;


        public Universe() {
            posX = RAND.nextInt(WIDTH);
            posY = 0;
            w = RAND.nextInt(5)+ 1;
            h = RAND.nextInt(5)+ 1;
            r = RAND.nextInt(100)+ 150;
            g = RAND.nextInt(100)+ 150;
            b = RAND.nextInt(100)+ 150;
            opacity = RAND.nextFloat();
            if(opacity < 0)opacity *= -1;
            if(opacity > 0.5)opacity = 0.5;


        }
        public void draw() {
            if(opacity > 0.8)opacity -= 0.01;
            if(opacity < 0.1)opacity += 0.01;

            gc.setFill(Color.LIGHTPINK);
            gc.fillOval(posX, posY, w, h);
            posY += 20;

        }


    }

    Bomb newBomb() {
        return new Bomb(50 + RAND.nextInt(WIDTH - 100), 0 , PLAYER_SIZE,
                BOMBS_IMG[RAND.nextInt(BOMBS_IMG.length)]);
    }
    int distance(int x1, int y1, int x2, int y2) {
        return(int)Math.sqrt(Math.pow((x1 - x2), 2)+ Math.pow((y1 - y2), 2));
    }
    public static void main(String[] args) {
        launch();
    }
}



