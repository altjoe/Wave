import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Waves extends PApplet {

float speeddiv = 20;
float wavemagmax;
ArrayList<Wave> waves = new ArrayList<Wave>();
public void setup() {
    
    background(255);
    wavemagmax = sqrt(width*width + height*height);

    // new_wave();
    for (int i = 0; i < 2; i++){
        new_wave();
    }
}   

int wavedirections = 30;
int wavecount = 0;
public void new_wave(){
    float angle = 2*PI/wavedirections;
    angle *= wavecount;//int(random(0, wavedirections + 1));
    PVector loc = circlexy(angle, wavemagmax/2);
    Wave wave = new Wave(loc.x, loc.y, angle);
    waves.add(wave);
    wavecount += 1;
}

public PVector circlexy(float angle, float radius){
    float x = radius * cos(angle) + width/2;
    float y = radius * sin(angle) + height/2;
    PVector loc = new PVector(x, y);
    return loc;
}
int newwavecount = 0;
int newwaveinterval = 15;
public void draw() {
    background(255);
    for (int i = waves.size()-1; i >= 0; i--) {
        Wave wave = waves.get(i);
        if (!wave.finished){
            wave.move();
            wave.display();
        } else {
            waves.remove(i);
        } 
    }
    if (newwavecount > newwaveinterval){
        new_wave();
        newwavecount = 0;
        newwaveinterval = PApplet.parseInt(random(15, 30));
    }

    newwavecount += 1;
}

class FadeLines {
    PVector[] points;
    int frames = 60;
    float sw;
    int alivecount;
    boolean dead = false;
    public FadeLines(ArrayList<PVector> lst, float sw){
        points = new PVector[lst.size()];
        for (int i = 0; i < lst.size(); i++) {
            points[i] = lst.get(i);
        }
        this.sw = sw;
        alivecount = frames;
    }

    public void display(){
        if (alivecount > 0){
            noFill();
            float perc = PApplet.parseFloat(alivecount) / PApplet.parseFloat(frames);
            strokeWeight(sw * perc);
            stroke(0,0,0, 255 * perc);
            beginShape();
            for (PVector point : points) {
                curveVertex(point.x, point.y);
            }
            endShape();
            alivecount -= 1;
        } else {
            dead = true;
        }
        
    }
}

class Wave {
    PVector loc;
    PVector dim;
    float gravity = 0.175f/speeddiv;
    float rotation;
    int segments = 40;
    ArrayList<PVector> points = new ArrayList<PVector>();
    ArrayList<PVector> prev_points = new ArrayList<PVector>();
    boolean starting = true;
    boolean finished = false;
    float sw;

    ArrayList<FadeLines> fadelines = new ArrayList<FadeLines>();

    public Wave(float x, float y, float r){     // rotation at 0 gravity goes down at PI goes up
        r += PI/2;
        loc = new PVector(x, y);
        rotation = r;
        startwave();
        sw = random(1, 2.5f);
    }

    public Wave(PVector loc0, float r){     // rotation at 0 gravity goes down at PI goes up
        loc = loc;
        rotation = r;
        startwave();
    }

    public Wave(float x, float y, float r, int s){
        loc = new PVector(x, y);
        rotation = r;
        segments = s;
    }

    public void startwave(){
        for (int i = 0; i <= segments; i++){
            float x = wavemagmax * i / (segments - 3) - wavemagmax*2/(segments-3);
            x -= wavemagmax/2;
            float y = -random(5, 50);
            if (PApplet.parseInt(random(0, 2)) == 1) {
                y -= i * 2;
            } else {
                y += i * 2;
            }

            float force = random(30, 50)/speeddiv;
            prev_points.add(new PVector(x, y - force));
            points.add(new PVector(x, y));
        }
        convolve(2);
    }

    public boolean visible(){
        for (int i = 0; i < points.size(); i++){
            if (points.get(i).y >= 0) {
                return true;
            }
        }
        return false;
    }

    int fadeinterval = 7;
    int fadecounter = 0;
    public void display(){
        pushMatrix();
        translate(loc.x, loc.y);
        rotate(rotation);
        for (int i = fadelines.size()-1; i >= 0; i--){
            FadeLines line = fadelines.get(i);
            if (!line.dead){
                line.display();
            } else {
                fadelines.remove(i);
            }
            
        }
        stroke(0);
        strokeWeight(sw);
        noFill();
        beginShape();
        for (PVector point : points) {
            curveVertex(point.x, point.y);
        }
        endShape();
        popMatrix();
        
        if (!visible() && !starting){
            finished = true;
        } else if (visible() && starting) {
            starting = false;
        }

        if (fadecounter > fadeinterval) {
            FadeLines line = new FadeLines(points, sw);
            fadelines.add(line);
            fadecounter = 0;
        }
        fadecounter += 1;
    }

    public void move(){
        for (int i = 0; i < points.size(); i++) {
            PVector current = points.get(i);
            PVector previous = prev_points.get(i);
            PVector diff = PVector.sub(current, previous);
            prev_points.set(i, current);
            current = PVector.add(current, diff);
            current.y -= gravity;
            points.set(i, current);
        }
    }

    public void convolve(int times){
        for (int i = 0; i < times; i++){
            for (int j = 1; j < points.size(); j++){
                PVector prev = points.get(j-1);
                PVector current = points.get(j);
                current.y = (prev.y + current.y) / 2;
                points.set(j, current);

                PVector prev0 = prev_points.get(j-1);
                PVector current0 = prev_points.get(j);
                current0.y = (prev0.y + current0.y) / 2;
                prev_points.set(j, current0);
            }
        }
        
    }
}
  public void settings() {  size(512, 512); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Waves" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
