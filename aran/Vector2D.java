package aran;

import battlecode.common.Direction;
import battlecode.common.MapLocation;

//http://www.cs.cityu.edu.hk/~cssamk/pinball/Vector2D.java

class Vector2D
{
      double x, y;
      Vector2D()
      {
          x = y = 0;      
      };
      Vector2D(double _x, double _y)
      {
          x = _x;
          y = _y;      
      };
      Vector2D(MapLocation maploc){
    	  this.x= maploc.x;
    	  this.y= maploc.y;
      }
      MapLocation getMapLoc(){
    	  return new MapLocation((float) x, (float) y);
      }
      Vector2D(double rad){
    	  x= Math.cos(rad);
    	  y= Math.sin(rad);
      }
      double length()
      {
            return Math.sqrt(x*x+y*y);      
      };
    
      Vector2D add(Vector2D v)
      {
          //return new Vector2D(x+v.x,y+v.y);      
    	  x+= v.x;
    	  y+= v.y;
    	  return this;
      }; 
      Vector2D minus(Vector2D v)
      {
          x-= v.x;
          y-= v.y;
          return this;
      };  
      Vector2D normalize()
      {
          double len = length();
          if( len != 0 )
          {
              x /= len;
              y /= len;
          } 
          return this;
      };      
      void setLength(double value)
      {
          normalize();
          x *= value;
          y *= value;      
      };
      Vector2D scale(double f)
      {
           double x2 = x;
           double y2 = y;
           normalize();
           x = x2*f;
           y = y2*f; 
           return this;
      };
      
      void rotate(double theta, double cx, double cy)
      {
           double sin = Math.sin(theta);
           double cos = Math.cos(theta);       
           double dx = x-cx;
           double dy = y-cy;
           x = cx+(dx)*cos-(dy)*sin;
           y = cy+(dy)*cos+(dx)*sin;      
      };
//      Vector2D normal()
//      { 
//           return new Vector2D(x,-y);      
//      };

      public String toStr(){
    	  return "{"+ String.valueOf(x)+ " , " + String.valueOf(y)+ "}";
      }
      
      // the dot product implemented as class function
      static double dot(Vector2D v1, Vector2D v2)
      {
             return v1.x*v2.x+v1.y*v2.y;     
      };                 
};