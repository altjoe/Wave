ArrayList<Wave> waves;
float speeddiv = 10;
void setup() {
    size(512, 512);
    background(255);
    speeddiv = 1.0 / speeddiv;
    waves = new ArrayList<Wave>();
    Wave wave = new Wave();
    waves.add(wave);
}
int newwave = 100;
int newwavecount = 0;
void draw() {
    background(255);
    for (int i = waves.size() - 1; i >= 0; i--){
        if (!waves.get(i).finished){
            // println("Ran");
            waves.get(i).move();
            waves.get(i).display();
        } else {
            waves.remove(i);
        }
    }
    if (newwavecount > newwave){
        Wave wave = new Wave();
        waves.add(wave);
        newwavecount = 0; 
        newwave = int(random(100, 200));
    }
    newwavecount += 1;
}

class FadeCurve {
    int fadetime = int(random(60,120));
    int display_count = 0;
    boolean finished = false;
    PVector[] curve;

    public FadeCurve(PVector[] c){
        curve = c;
    }

    void display() {
        if (display_count < fadetime){
            noFill();
            float perc = float(fadetime - display_count) / float(fadetime);
            stroke(0, 0, 0, 255.0 * (perc));
            beginShape();
            for (int j = 0; j < curve.length; j++){
                curveVertex(curve[j].x, curve[j].y);
            }
            endShape();
            display_count += 1;
        } else {
            finished = true;
        }
        
    }
}

class Wave {
    float gravity = -random(1, 2.5);
    float turbulance = 2;
    ArrayList<Verlet> wave_line = new ArrayList<Verlet>();
    ArrayList<FadeCurve> wave_lines = new ArrayList<FadeCurve>();
    int wave_length = 30;
    boolean left = true;
    boolean finished = false;
    boolean juststarted = true;

    public Wave() {
        if (int(random(0,2)) == 1){
            left = false;
        }
        start_wave();
    }

    void start_wave() {

        for (int i = 0; i < wave_length; i++){
            float x = i*width/(wave_length - 3) - width/(wave_length - 3);
            if (left){
                PVector point = new PVector(x, random(-45-i*2, -5-i*2));
                PVector force = new PVector(0, random(10*speeddiv, 20*speeddiv));
                Verlet ver = new Verlet(point, force);
                wave_line.add(ver);
            } else {
                PVector point = new PVector(x, random(-60+i*2, -30+i*2));
                PVector force = new PVector(0, random(10*speeddiv, 20*speeddiv));
                Verlet ver = new Verlet(point, force);
                wave_line.add(ver);
            }
            
        }

        for (int j = 0; j <= 1; j++){
            for (int i = 1; i < wave_line.size(); i++){
                Verlet prev = wave_line.get(i-1);
                wave_line.get(i).convolve(prev);
            }
        }
        if (visible()){
            PVector[] arr = snapshot();
            FadeCurve curve = new FadeCurve(arr);
            wave_lines.add(curve);
        }
       
    }



    void display() {
        for (int i = wave_lines.size()-1; i >= 0; i--){
            FadeCurve curve = wave_lines.get(i);
            if (!curve.finished){
                curve.display();
            } else {
                wave_lines.remove(i);
            }
        }
        noFill();
        stroke(0);
        beginShape();
        for (int j = 0; j < wave_line.size(); j++){
            curveVertex(wave_line.get(j).current.x, wave_line.get(j).current.y);
        }
        endShape();

        if (wave_lines.size() == 0 && !juststarted){
            finished = true;
        } else if (visible() && juststarted){
            juststarted = false;
        }
    }
    int count = 0;
    int drawn_line_freq = 8;

    void move() {
        for (int i = 0; i < wave_line.size(); i++){
            wave_line.get(i).next();
        }

        count += 1;
        if (count >= drawn_line_freq && visible()){
            PVector[] arr = snapshot();
            FadeCurve curve = new FadeCurve(arr);
            wave_lines.add(curve);
            count = 0;
        }
    }

    PVector[] snapshot(){
        PVector[] arr = new PVector[wave_line.size()];
        for (int i = 0; i < wave_line.size(); i++){
            arr[i] = wave_line.get(i).current;
        }
        return arr;
    }

    boolean visible(){
        for (int i = 0; i < wave_line.size(); i++){
            if (wave_line.get(i).current.y > 0){
                return true;
            }
        }
        return false;
    }
}

class Verlet {
    PVector prev;
    PVector current;
    PVector gravity = new PVector(0, -0.05 * speeddiv); 

    public Verlet(PVector pos, PVector force){
        current = pos;
        prev = PVector.sub(current, force);
    }

    void next(){
        PVector diff = PVector.sub(current, prev);
        diff = PVector.add(diff, gravity);
        prev = current;
        current = PVector.add(current, diff);
    }

    void convolve(Verlet v) {
        float prevx = prev.x;
        prev = PVector.div(PVector.add(v.prev, prev), 2.0);
        current = PVector.div(PVector.add(v.current, current), 2.0);
        prev.x = prevx;
        current.x = prevx;
    }
}

/*
The farther away from the start the weaker the force acting upon it is 
    - can be gravity 
    - might look into distance related force
Would like turbulance of previous wave and new one 
    - can be force used similar to gravity at certain point
        - point can be when it has pass previous wave?
a

*/