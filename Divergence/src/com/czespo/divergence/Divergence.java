/**
 *  Divergence (a Sokoban (or Sokouban if you're a purist) clone)
 *  Copyright (C) 2020 Czespo
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *  
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.czespo.divergence;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class Divergence extends JPanel
{
    // STATIC //
    
    static final int LEFT = KeyEvent.VK_LEFT;
    static final int UP = KeyEvent.VK_UP;
    static final int RIGHT = KeyEvent.VK_RIGHT;
    static final int DOWN = KeyEvent.VK_DOWN;
    static final int KEY_R = KeyEvent.VK_R;
    
    static ArrayList<String> levels = new ArrayList<String>();
    
    public static void main(String[] args)
    {
        if(!initLevels()) return;
        
        boolean fullscreen = true;
        
        // Allow a `-w` flag to launch in windowed mode.
        // Can be followed by width and height: `-w 800 600`.
        // Defaults to 800x600.
        int width = 800, height = 600;
        for(int i = 0; i < args.length; i++)
        {
            if(args[i].equals("-w"))
            {
                fullscreen = false;
                if(i + 2 < args.length)
                {
                    width = Integer.parseInt(args[i + 1]);
                    height = Integer.parseInt(args[i + 2]);
                }
            }
        }
        
        JFrame frame = new JFrame("Divergence");
        if(fullscreen)
        {
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setUndecorated(true);
        }
        else
        {
            frame.setSize(width, height);
        }
        
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        
        Divergence divergence = new Divergence(frame.getWidth(), frame.getHeight());
        
        frame.add(divergence);
        frame.pack();
        
        // Request focus, so that the panel may receive key events.
        divergence.requestFocus();
    }
    
    public static boolean initLevels()
    {
        // Load definition strings of Divergence
        // levels from the level file.
        try
        {
            Scanner levelFile = new Scanner(new File("levels"));
            
            String level = "";
            while(levelFile.hasNextLine())
            {
                String line = levelFile.nextLine();
                if(line.equals(","))
                {
                    levels.add(level.substring(0, level.length() - 1));
                    level = "";
                }
                else
                {
                    level += line + "|";
                }
            }

            levelFile.close();

            return true;
        }
        catch(FileNotFoundException e)
        {
            System.err.println("Error: could not open 'levels'!");
            
            return false;
        }
    }
    
    public static Point move(int direction, Point src)
    {
        switch(direction)
        {
            case LEFT: return new Point(src.x - 1, src.y);
            case UP: return new Point(src.x, src.y - 1);
            case RIGHT: return new Point(src.x + 1, src.y);
            case DOWN: return new Point(src.x, src.y + 1);
        }
        
        return null;
    }
    
    // FIELDS //
    
    int wWidth, wHeight;

    int cell, xp, yp;
    
    int levelNum = 0;
    
    // OBJECTS //
    
    Level currentLevel;
    
    // CONSTUCTORS //
    
    public Divergence(int width, int height)
    {
        // Set the wWidth and wHeight variables.
        wWidth = width;
        wHeight = height;
        
        // Load first level.
        currentLevel = loadLevel(levels.get(levelNum));
        
        this.setFocusable(true);
        this.setBackground(Color.BLACK);
        
        this.addKeyListener(new KeyHandler());
        
        this.setBounds(0, 0, width, height);
        this.setPreferredSize(new Dimension(width, height));
        
        this.setVisible(true);
    }
    
    // INNER CLASSES //
    
    private class KeyHandler extends KeyAdapter
    {
        @Override
        public void keyPressed(KeyEvent e)
        {
            int key = e.getKeyCode();
            switch(key)
            {
                case LEFT:
                case UP:
                case RIGHT:
                case DOWN:
                    // Move the player, if possible.
                    // If level is complete, load the next one.
                    if(update(key))
                    {
                        if(++levelNum < levels.size())
                        {
                            currentLevel = loadLevel(levels.get(levelNum));
                        }
                        else
                        {
                            System.out.println("All levels completed.");
                            System.exit(0);
                        }
                    }
                    break;
                    
                case KEY_R:
                    // Restart the current level.
                    currentLevel = loadLevel(levels.get(levelNum));
                    break;
                
                case KeyEvent.VK_ESCAPE:
                    System.exit(0);
            }
        }
    }
    
    // METHODS //
    
    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        // Do drawing.

        // Used to draw goals, which need to be comparatively smaller than boxes.
        int quarter = cell / 4;

        for(int y = 0; y < currentLevel.height; y++)
        {
            for(int x = 0; x < currentLevel.map.get(y).size(); x++)
            {
                if(currentLevel.get(x, y).type == Cell.FLOOR)
                {
                    if(currentLevel.get(x, y).hasBox)
                    {
                        // Determine what colour the box should be.
                        // If the box is on a goal, draw it in green
                        // to differentiate it from other boxes.
                        if(currentLevel.get(x, y).isGoal)
                        {
                            g.setColor(Color.GREEN);
                        }
                        else
                        {
                            g.setColor(Color.RED);
                        }

                        // Draw the boxes.
                        g.fillRect(x * cell + xp, y * cell + yp, cell - 1, cell - 1);
                    }
                    else if(currentLevel.get(x, y).isGoal)
                    {
                        // Draw the goals.
                        g.setColor(Color.RED);
                        g.fillRect(x * cell + quarter + xp, y * cell + quarter + yp, cell - quarter * 2 - 1, cell - quarter * 2 - 1);
                    }
                }
                else
                {
                    // Draw the walls.
                    g.setColor(Color.WHITE);
                    g.fillRect(x * cell + xp, y * cell + yp, cell - 1, cell - 1);
                }
            }
        }

        // Draw the player.
        g.setColor(Color.BLUE);
        g.fillRect(currentLevel.player.x * cell + xp, currentLevel.player.y * cell + yp, cell - 1, cell - 1);
    }
    
    // FUNCTIONS //
    
    public Level loadLevel(String definition)
    {
        // Create a Divergence level from a definition string.
        Level level = new Level();
        level.map.add(new ArrayList<Cell>());

        int width = 0, height = 0, x = 0;
        for(int i = 0; i < definition.length(); i++)
        {
            switch(definition.charAt(i))
            {
                case '.': // Goal.
                    level.map.get(height).add(new Cell(Cell.FLOOR, true, false));
                    level.goals++;
                    break;

                case '$': // Box.
                    level.map.get(height).add(new Cell(Cell.FLOOR, false, true));
                    break;

                case '*': // Box over goal.
                    level.map.get(height).add(new Cell(Cell.FLOOR, true, true));
                    break;

                case '#': // Wall.
                    level.map.get(height).add(new Cell(Cell.WALL, false, false));
                    break;

                case '@': // Player.
                    level.player = new Point(x, height);
                    level.map.get(height).add(new Cell(Cell.FLOOR, false, false));
                    break;
                    
                case '&': // Player over a goal.
                    level.player = new Point(x, height);
                    level.map.get(height).add(new Cell(Cell.FLOOR, true, false));
                    level.goals++;
                    break;

                case '|': // Start a new row.
                    height++;
                    if(x > width) width = x;

                    x = -1;
                    level.map.add(new ArrayList<Cell>());
                    break;

                default: // Empty floor.
                    level.map.get(height).add(new Cell(Cell.FLOOR, false, false));
                    break;
            }

            x++;
        }
        
        level.width = width;
        level.height = ++height;
        
        // Determine cell size based on board and window dimensions.
        // Allows the drawn board to scale to the window size.
        cell = Math.min(wWidth / width, wHeight / height);

        // Determine x and y padding, which are
        // used to centre the level within the window.
        xp = (wWidth - (cell * width)) / 2;
        yp = (wHeight - (cell * height)) / 2;
        
        return level;
    }

    public boolean update(int direction)
    {
        Point dest = move(direction, currentLevel.player);
        if(currentLevel.get(dest).type != Cell.WALL)
        {
            // If the player moves into a box, we try to push that box.
            if(currentLevel.get(dest).hasBox && moveBox(direction, dest))
            {
                currentLevel.player = dest;
                
                // Request drawing.
                this.repaint();

                // Check if the level has been completed.
                if(currentLevel.goals == 0)
                {
                    return true;
                }
            }
            else if(!currentLevel.get(dest).hasBox)
            {
                currentLevel.player = dest;

                // Request drawing.
                this.repaint();
            }
        }

        return false;
    }

    public boolean moveBox(int direction, Point src)
    {
        // We move the box if the destination does not
        // contain a wall or another box.
        Point dest = move(direction, src);
        if(currentLevel.get(dest).type != Cell.WALL && !currentLevel.get(dest).hasBox)
        {
            currentLevel.get(src).hasBox = false;
            currentLevel.get(dest).hasBox = true;

            // Increment remaining goals if the box was pushed off a goal.
            if(currentLevel.get(src).isGoal) currentLevel.goals++;

            // Decrement remaining goals if the box was pushed onto a goal.
            if(currentLevel.get(dest).isGoal) currentLevel.goals--;

            return true;
        }

        return false;
    }
}
