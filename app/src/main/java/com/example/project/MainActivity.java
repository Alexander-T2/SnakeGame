package com.example.project;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private SurfaceView surfaceView;
    private TextView scoreTV;

    private SurfaceHolder surfaceHolder; // draws snake
    private final List<SnakePoints> snakePointsList = new ArrayList<>();

    private String movingPosition = "right";
    private int score = 0;
    private int positionX, positionY; // Point's X&Y pos

    private static final int pointsize = 28;
    private static final int defaultTalePoints = 5;
    private static final int snakeColor = Color.RED;
    private static final int snakeMovingSpeed = 800;

    private Timer timer;

    private Canvas canvas = null;

    private Paint pointColor = null; // snake color
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        surfaceView = findViewById(R.id.surfaceView);
        scoreTV = findViewById(R.id.scoreTV);

        final AppCompatImageButton topBtn = findViewById(R.id.topBtn);
        final AppCompatImageButton bottomBtn = findViewById(R.id.bottomBtn);
        final AppCompatImageButton leftBtn = findViewById(R.id.leftBtn);
        final AppCompatImageButton rightBtn = findViewById(R.id.rightBtn);

        surfaceView.getHolder().addCallback(this);
        topBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!movingPosition.equals("bottom")){
                    movingPosition="top";
                }
            }
        });

        leftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!movingPosition.equals("right")) {
                    movingPosition = "left";
                }
            }
        });

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!movingPosition.equals("left")) {
                    movingPosition = "right";
                }
            }
        });

        bottomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!movingPosition.equals("top")) {
                    movingPosition = "bottom";
                }
            }
        });
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        this.surfaceHolder = surfaceHolder;

        init();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    private void init(){
        snakePointsList.clear();
        scoreTV.setText("0");
        score = 0;
        movingPosition = "right";
        int startPositionX = (pointsize) * defaultTalePoints;

        for(int i = 0; i < defaultTalePoints; i++) {
            // longering snake
            SnakePoints snakePoints = new SnakePoints(startPositionX, pointsize);
            snakePointsList.add(snakePoints);

            startPositionX = startPositionX - (pointsize * 2);
        }
        addPoint();

        moveSnake(); // START moving snake
    }

    private void addPoint(){
        int surfaceWidth = surfaceView.getWidth() - (pointsize * 2);
        int surfaceHeight = surfaceView.getHeight() - (pointsize * 2);

        int randomXPosition = new Random().nextInt(surfaceWidth / pointsize);
        int randomYPosition = new Random().nextInt(surfaceHeight / pointsize);

        if((randomXPosition % 2) != 0){
            randomXPosition = randomXPosition + 1;
        }

        if((randomYPosition % 2) != 0){
            randomYPosition = randomYPosition + 1;
        }

        positionX = ((pointsize * randomXPosition) + pointsize) % surfaceWidth;
        positionY = ((pointsize * randomYPosition) + pointsize) % surfaceHeight;
    }

    private void moveSnake(){

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // getting head position
                int headPositionX = snakePointsList.get(0).getPositionX();
                int headPositionY = snakePointsList.get(0).getPositionY();
                // check if point is eaten
                if((Math.abs(headPositionX - positionX) <= 28) && (Math.abs(positionY - headPositionY) < 28)){
                    growSnake();
                    addPoint();
                }

                switch (movingPosition){
                    case "right":
                        snakePointsList.get(0).setPositionX(headPositionX + pointsize * 2);
                        snakePointsList.get(0).setPositionY(headPositionY);
                        break;
                    case "left":
                        snakePointsList.get(0).setPositionX(headPositionX - pointsize * 2);
                        snakePointsList.get(0).setPositionY(headPositionY);
                        break;
                    case "top":
                        snakePointsList.get(0).setPositionX(headPositionX);
                        snakePointsList.get(0).setPositionY(headPositionY - pointsize * 2);
                        break;
                    case "bottom":
                        snakePointsList.get(0).setPositionX(headPositionX);
                        snakePointsList.get(0).setPositionY(headPositionY + pointsize * 2);
                        break;

                }

                if(checkGameOver(headPositionX, headPositionY)){
                    // stop timer and snake
                    timer.purge();
                    timer.cancel();

                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("а-аа, пацаны, я маслину поймал!");
                    builder.setMessage("score:" + score);
                    builder.setCancelable(false);
                    builder.setPositiveButton("Start Again", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // game restart
                            init();
                        }
                    });
                    // connecting dialog window to main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            builder.show();
                        }
                    });
                }
                else{
                    canvas = surfaceHolder.lockCanvas(); // locks canvas to draw on it
                    canvas.drawColor(Color.WHITE, PorterDuff.Mode.CLEAR); // clear canvas with white
                    // changes head position
                    canvas.drawCircle(snakePointsList.get(0).getPositionX(), snakePointsList.get(0).getPositionY(), pointsize,createPointColor());
                    canvas.drawCircle(positionX, positionY, pointsize, createPointColor()); // random food

                    // other bodyparts follow
                    for(int i=1; i < snakePointsList.size(); i++){
                        int getTempPositionX = snakePointsList.get(i).getPositionX();
                        int getTempPositionY = snakePointsList.get(i).getPositionY();
                        // moving bodyparts
                        snakePointsList.get(i).setPositionX(headPositionX);
                        snakePointsList.get(i).setPositionY(headPositionY);
                        canvas.drawCircle(snakePointsList.get(i).getPositionX(), snakePointsList.get(i).getPositionY(), pointsize, createPointColor());

                        // changing head position
                        headPositionX = getTempPositionX;
                        headPositionY = getTempPositionY;
                    }

                    surfaceHolder.unlockCanvasAndPost(canvas); // unlock canvas to draw on surface
                }
            }
        },1000 - snakeMovingSpeed, 1000 - snakeMovingSpeed);
    }

    private void growSnake(){
        SnakePoints snakePoints = new SnakePoints(0, 0);
        snakePointsList.add(snakePoints);
        score++;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                scoreTV.setText(String.valueOf(score));
            }
        });
    }

    private boolean checkGameOver(int headPositionX, int headPositionY){
        boolean gameOver = false;
        if(snakePointsList.get(0).getPositionX() < 0 ||
                snakePointsList.get(0).getPositionY() < 0 ||
        snakePointsList.get(0).getPositionX() >= surfaceView.getWidth() ||
        snakePointsList.get(0).getPositionY() >= surfaceView.getHeight()){
            gameOver = true;
        }
        else{
            for(int i = 1; i<snakePointsList.size(); i++){
                if(headPositionX == snakePointsList.get(i).getPositionX() &&
                headPositionY == snakePointsList.get(i).getPositionY()){
                    gameOver = true;
                }
            }
        }
        return gameOver;
    }

    private Paint createPointColor(){
        if(pointColor == null){
            pointColor = new Paint();
            pointColor.setColor(snakeColor);
            pointColor.setStyle(Paint.Style.FILL);
            pointColor.setAntiAlias(true); // makes everything smooth
        }
        return pointColor;
    }
}