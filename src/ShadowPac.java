import bagel.*;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import bagel.util.Point;
import bagel.util.Rectangle;

import static java.lang.Math.PI;
/**
 * Skeleton Code for SWEN20003 Project 1, Semester 1, 2023
 *
 * Please enter your name below
 * @author: Taeeun Kim
 * Student ID: 1351062
 */
public class ShadowPac extends AbstractGame  {
    private final static int WINDOW_WIDTH = 1024;
    private final static int WINDOW_HEIGHT = 768;
    private final static String GAME_TITLE = "SHADOW PAC";
    private final Image BACKGROUND_IMAGE = new Image("../res/background0.png");
    private final Image ghostImage = new Image("../res/ghostRed.png");
    private final Image wallImage = new Image("../res/wall.png");
    private final Image dotImage = new Image("../res/dot.png");
    private final Image heartImage = new Image("../res/heart.png");
    private final Image pacClosed = new Image("../res/pac.png");
    private final Image pacOpen = new Image("../res/pacOpen.png");
    // we will use arrays here so it is easy to read the csv file and add the elements
    List<Dot> dots = new ArrayList<>();
    List<Ghost> ghosts = new ArrayList<>();
    List<Wall> walls = new ArrayList<>();
    List<Player> players = new ArrayList<>();
    String state = "Start";

    public ShadowPac(){
        super(WINDOW_WIDTH, WINDOW_HEIGHT, GAME_TITLE);
    }

    /**
     * Method used to read file and create objects (you can change
     * this method as you wish).
     */
    public void CSVReader () {
        try {
            File file = new File("../res/level0.csv");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String[] values = line.split(",");

                String type = values[0];
                int x = Integer.parseInt(values[1]);
                int y = Integer.parseInt(values[2]);
                switch (type) {
                    // have an type array of the types. We will store info of each type in this array.
                    case "Ghost":
                        Ghost ghost = new Ghost(x, y, new Rectangle(x,y,25,25));
                        ghosts.add(ghost);
                        break;

                    case "Wall":
                        Wall wall = new Wall(x, y, new Rectangle(x,y,50,50));
                        walls.add(wall);
                        break;

                    case "Dot":
                        Dot dot = new Dot(x, y, new Rectangle(x, y, 26, 26));
                        dots.add(dot);
                        break;

                    case "Player":
                        Player player = new Player(x, y,new Rectangle(x, y, 25, 25));
                        players.add(player);
                        break;
                }
            }



        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The entry point for the program.
     */
    public static void main(String[] args) {
        ShadowPac game = new ShadowPac();
        game.CSVReader();
        game.run();
    }

    /**
     * Performs a state update.
     * Allows the game to exit when the escape key is pressed.
     */
    @Override
    protected void update(Input input) {
        int frameToUpdate;
        double degree = 0;
        int life;

        Font font = new Font("../res/FSO8BITR.TTF", 64);

        Image pacClosed = new Image("../res/pac.png");
        Image pacOpen = new Image("../res/pacOpen.png");
        Image pac = pacClosed;
        DrawOptions drawOptions = new DrawOptions().setRotation(degree);

        if (input.wasPressed(Keys.ESCAPE)){
            Window.close();
        }
        // just the start screen
        BACKGROUND_IMAGE.draw(Window.getWidth()/2.0, Window.getHeight()/2.0);
        if(state.equals("Start")) {
            font.drawString("SHADOW PAC", 260, 250);
            font = new Font("../res/FSO8BITR.TTF", 24);
            font.drawString("PRESS SPACE TO START\nUSE ARROW KEYS TO MOVE", 260 + 60, 250 + 190);
        }

        // start page to game in progress
        if (input.wasPressed(Keys.SPACE) && state.equals("Start")){
            state = "In progress";
        }

        if (state.equals("In progress")) {
            boolean checkMoving = false;

            // in progress to lose
            if ((players.get(0).heart).equals(0)){
                // game over
                state = "Game lost";
                return;
            }
            // in progress to win
            if ((players.get(0).score).equals(1210)) {
                state = "Game won";
                return;
            }

            // wall, dot, ghost classes are added.
            for (Wall wall:walls) {
                wall.Draw(wallImage);
            }
            for (Dot dot:dots) {
                if (dot.eaten == false) {
                    dot.Draw(dotImage);
                }

            }
            for (Ghost ghost:ghosts) {
                ghost.Draw(ghostImage);
            }
            // we only want one keyboard input at once. no moving diagonally.
            // player class is added - this is to keep track of the direction
            if (input.isDown(Keys.LEFT)) {
                players.get(0).direction = "Left";
                checkMoving = true;
            }
            if (input.isDown(Keys.RIGHT)) {
                players.get(0).direction = "Right";
                checkMoving = true;
            }
            if (input.isDown(Keys.UP)) {
                players.get(0).direction = "Up";
                checkMoving = true;
            }
            if (input.isDown(Keys.DOWN)) {
                players.get(0).direction = "Down";
                checkMoving = true;
            }
            if (checkMoving == true) {
                touched(players.get(0), walls, ghosts, dots);
            }

            // we keep track of the frame on the player class. Update it everytime it goes through update()
            frameToUpdate = players.get(0).frame;
            players.get(0).frame += 1;
            // from 1 to 15: open mouth, 16 to 30: close mouth. set back to 0 when above 30.
            if (0 <= frameToUpdate && frameToUpdate < 16) {
                pac = pacOpen;
            }
            if (frameToUpdate >= 16 && frameToUpdate < 31) {
                pac = pacClosed;
            }
            if (frameToUpdate > 30) {
                players.get(0).frame = 0;
            }

            // update degrees and rotate accordingly.
            if (players.get(0).direction.equals("Right")) {
                degree = 0;
                drawOptions = new DrawOptions().setRotation(degree);
                pac.drawFromTopLeft(players.get(0).x, players.get(0).y, drawOptions);
                // System.out.println(">>>>>>");
            }
            if (players.get(0).direction.equals("Left")) {
                degree = PI;
                drawOptions = new DrawOptions().setRotation(degree);
                pac.drawFromTopLeft(players.get(0).x, players.get(0).y, drawOptions);
                // System.out.println("<<<<<<<<<<<");
            }
            if (players.get(0).direction.equals("Up")) {
                degree = PI + PI/2;
                drawOptions = new DrawOptions().setRotation(degree);
                pac.drawFromTopLeft(players.get(0).x, players.get(0).y, drawOptions);
                // System.out.println("^^^^^^^^^^^^^");
            }
            if (players.get(0).direction.equals("Down")) {
                degree = PI/2;
                drawOptions = new DrawOptions().setRotation(degree);
                pac.drawFromTopLeft(players.get(0).x, players.get(0).y, drawOptions);

                // System.out.println("downnnnnnnnn");
            }

            // here we create score
            font = new Font("../res/FSO8BITR.TTF", 20);
            font.drawString("SCORE", 25, 25);
            font.drawString(Integer.toString(players.get(0).score), 25+78.125+12.5, 25);

            // here we create hearts - we can access to number of hearts through players.get(0).heart
            life = players.get(0).heart;
            switch(life){
                case (3):
                    heartImage.drawFromTopLeft(900, 10);
                    heartImage.drawFromTopLeft(900+30, 10);
                    heartImage.drawFromTopLeft(900+30+30, 10);
                case (2):
                    heartImage.drawFromTopLeft(900, 10);
                    heartImage.drawFromTopLeft(900+30, 10);
                case (1):
                    heartImage.drawFromTopLeft(900, 10);
            }

        }

        if (state.equals("Game won")) {
            font.drawString("WELL DONE!", 280, 400);
        }

        if (state.equals("Game lost")) {
            font.drawString("GAME OVER!", 280, 400);
        }

    }

    public void touched(Player player, List <Wall> walls, List <Ghost> ghosts, List <Dot> dots) {
        // System.out.println("touched - - - - - - -");
        int orgX = player.x;
        int orgY = player.y;

        int changedX = 0;
        int changedY = 0;

        // change the position of the hitbox which will test if it can move or not.
        // change its position according to the input.
        switch (player.direction) {
            case "Right":
                // this is +3 bc it's 3 pixels per frame.
                changedX = player.x+3;
                changedY = player.y;
                player.hitbox.moveTo(new Point(changedX, changedY));
                // System.out.println("RIGHT");
                break;

            case "Left":
                changedX = player.x-3;
                changedY = player.y;
                player.hitbox.moveTo(new Point(changedX, changedY));
                // System.out.println("LEFT");
                break;

            case "Up":
                changedX = player.x;
                changedY = player.y-3;
                player.hitbox.moveTo(new Point(changedX, changedY));
                // System.out.println("UP");
                break;

            case "Down":
                changedX = player.x;
                changedY = player.y+3;
                player.hitbox.moveTo(new Point(changedX, changedY));
                // System.out.println("DOWN");
                break;
        }

        System.out.println("Hitbox changed X: "+changedX);
        System.out.println("Hitbox changed Y: "+changedY);

        // if it hits the wall, then we don’t change the actual direction.
        // note we are assigning its position back to the original position.
        for(Wall wall: walls) {
            if (player.hitbox.intersects(wall.hitbox)) {
                player.hitbox.moveTo(new Point(orgX, orgY));
                System.out.println("Collided into wall");
                return;
            }
        }
        // if it hits the ghost, we will move the pacman to its starting position
        for(Ghost ghost: ghosts) {
            if(player.hitbox.intersects(ghost.hitbox)) {
                player.heart -= 1;
                player.x = 474;
                player.y = 662;
                player.hitbox.moveTo(new Point(player.x, player.y));
                // System.out.println("Collided into ghost");

                return;
            }
        }

        // we update the score if it is eaten. update its position too.
        for(Dot dot: dots) {
            if(player.hitbox.intersects(dot.hitbox)) {
                if(dot.eaten != true) {
                    player.score += 10;
                }
                dot.eaten = true;

                player.x = changedX;
                player.y = changedY;
                // System.out.println("Collided into dot");

                return;
            }
        }
        // no collide just update position
        System.out.println("X: "+player.x+" | Y: "+player.y+" ** BEFORE CHANGE **");
        player.x = changedX;
        player.y = changedY;
        System.out.println("X: "+player.x+" | Y: "+player.y+" ** AFTER CHANGE **");

    }

}