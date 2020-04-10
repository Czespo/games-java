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

public class Cell
{
    // STATIC //
    
    public static final int FLOOR = 0;
    public static final int WALL = 1;
    
    // FIELDS //
    
    public int type;
    
    public boolean isGoal, hasBox;
    
    // CONSTRUCTORS //
    
    public Cell(int type, boolean isGoal, boolean hasBox)
    {
        this.type = type;
        this.isGoal = isGoal;
        this.hasBox = hasBox;
    }
}
