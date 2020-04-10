/**
 *  Slither (a Snake clone)
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

package com.czespo.slither;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class Slither extends JPanel implements ActionListener
{
    // STATIC //
    
    static final int B_WIDTH = 20;
    static final int B_HEIGHT = 20;
    
    static final int DELAY = 1000 / 10;
    
    static final int LEFT = KeyEvent.VK_LEFT;
    static final int UP = KeyEvent.VK_UP;
    static final int RIGHT = KeyEvent.VK_RIGHT;
    static final int DOWN = KeyEvent.VK_DOWN;
    
    static final Color DARK_GREEN = new Color(0, 128, 0);
    
    static final Random random = new Random();
    
    public static void main(String[] args)
    {
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
        
        JFrame frame = new JFrame("Slither");
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
        
        Slither slither = new Slither(frame.getWidth(), frame.getHeight());
        
        frame.add(slither);
        
        frame.pack();
        
        // Request focus, so that the panel may receive key events.
        slither.requestFocus();
    }
    
    // FIELDS //
    
    private int wWidth, wHeight;
    
    private int cell, xp, yp;
    
    private int direction = RIGHT;
    
    private int length = 3;
    
    // OBJECTS //
    
    private Timer timer;
    
    private List<Point> body;
    
    private Point food;
    
    // CONSTRUCTORS //
    
    public Slither(int width, int height)
    {
        wWidth = width;
        wHeight = height;

        // Determine cell size based on board and window dimensions.
        // Allows the drawn board to scale to the window size.
        cell = Math.min(wWidth / B_WIDTH, wHeight / B_HEIGHT);

        // Determine x and y padding, so that we
        // can centre the board within the window.
        xp = (wWidth - (cell * B_WIDTH)) / 2;
        yp = (wHeight - (cell * B_HEIGHT)) / 2;
        
        // Initialise the snake body.
        body = new ArrayList<Point>();
        for(int k = 0; k < length; k++)
        {
            body.add(new Point(B_WIDTH / 2 - k, B_HEIGHT / 2));
        }

        // Initialise the food location.
        food = new Point(random.nextInt(B_WIDTH), random.nextInt(B_HEIGHT));
        
        this.setFocusable(true);
        this.setBackground(Color.GRAY);
        
        this.addKeyListener(new KeyHandler());
        
        this.setBounds(0, 0, width, height);
        this.setPreferredSize(new Dimension(width, height));
        
        this.setVisible(true);

        // Each time this timer fires, the
        // snake is moved and the window is drawn to.
        timer = new Timer(DELAY, this);
        timer.start();
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
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_UP:
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_DOWN:
                    direction = key;
                    break;
                
                case KeyEvent.VK_ESCAPE:
                    System.exit(0);
                    break;
            }
        }
    }
    
    // METHODS //
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        // Move the snake.
        int nx = body.get(0).x, ny = body.get(0).y;
        switch(direction)
        {
            case LEFT:
                nx += -1;
                break;

            case UP:
                ny += -1;
                break;

            case RIGHT:
                nx += 1;
                break;

            case DOWN:
                ny += 1;
                break;
        }

        // If the snake tries to go off the edge
        // of the board, wrap it around.
        if(nx >= B_WIDTH)
        {
            nx -= B_WIDTH;
        }
        else if(nx < 0)
        {
            nx += B_WIDTH;
        }
        else if(ny >= B_HEIGHT)
        {
            ny -= B_HEIGHT;
        }
        else if(ny < 0)
        {
            ny += B_HEIGHT;
        }

        // Move the snake by adding a new head.
        body.add(0, new Point(nx, ny));

        // Check if the snake is eating food.
        if(body.get(0).x == food.x && body.get(0).y == food.y)
        {
            // Increment length.
            length++;

            // Set food to a random location within the board's dimensions.
            food.x = random.nextInt(B_WIDTH);
            food.y = random.nextInt(B_HEIGHT);
        }
        else
        {
            // If the snake hasn't eaten, remove the tail.
            body.remove(body.size() - 1);
        }

        // Check if the snake is eating itself.
        // Start with the third part, since the
        // snake cannot eat any part before that.
        for(int i = 2; i < body.size(); i++)
        {
            if(body.get(0).x == body.get(i).x && body.get(0).y == body.get(i).y)
            {
                // Set length to 3 and trim body.
                length = 3;
                while(body.size() > length)
                {
                    body.remove(body.size() - 1);
                }

                break;
            }
        }
        
        // Request drawing.
        this.repaint();
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        // Do drawing.
        // Fill the board with black.
        g.setColor(Color.BLACK);
        g.fillRect(xp, yp, cell * B_WIDTH, cell * B_WIDTH);

        // Draw the snake's head, in dark green.
        g.setColor(DARK_GREEN);
        g.fillRect(body.get(0).x * cell + xp, body.get(0).y * cell + yp, cell - 1, cell - 1);

        // Draw the rest of the body, in green.
        g.setColor(Color.GREEN);
        for(int i = 1; i < body.size(); i++)
        {
            g.fillRect(body.get(i).x * cell + xp, body.get(i).y * cell + yp, cell - 1, cell - 1);
        }

        // Draw the food.
        g.setColor(Color.RED);
        g.fillRect(food.x * cell + xp, food.y * cell + yp, cell - 1, cell - 1);
        
        Toolkit.getDefaultToolkit().sync();
    }
}
