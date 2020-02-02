import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.LinkedList;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.UIManager;



	
class Hw5Panel extends JPanel { // 시작,게임,종료 화면을 넣을 메인패널 
	private int highScore;
	int myScore;
	Hw5PanelOpening Opening;
	Hw5PanelPlaying Playing;
	Hw5PanelEnding Ending;
	public void changeMode(String panelMode) {
		if(panelMode.equals("Opening")) {
			Opening = new Hw5PanelOpening(this);
			this.add(Opening);
			this.remove(Ending);
		}
		else if(panelMode.equals("Playing")) {
			Playing = new Hw5PanelPlaying(this);
			this.add(Playing);
			this.remove(Opening);
		}
		else if(panelMode.equals("Ending")) {
			Ending = new Hw5PanelEnding(this);
			this.add(Ending);
			this.remove(Playing);
		}
	}
	public void setScore(int score) {
		highScore=score;
	}
	public int getScore() {
		return highScore;
	}
	Hw5Panel(){
		highScore=0;
		setLayout(null);
		Opening = new Hw5PanelOpening(this);
		add(Opening);
	}
}

class Hw5SubPanel extends JPanel implements Runnable, KeyListener{ //시작,게임,종료 패널의 부모 클래스인 서브패널 
	int n;// "Press Space" 깜빡이게 
	Thread t= new Thread(this);
	String PanelName;
	Clip clip;
	Hw5Panel hw5Panel;
	Hw5SubPanel(Hw5Panel hw5Panel){
		this.hw5Panel = hw5Panel;
		this.setBounds(25, 0, 750, 800);
		setFocusable(true); //1
		requestFocus();//2 키보드,마우스 누른애가 포커스를 가짐 
		addKeyListener(this); //3 이 세트 
		t.start();
	}	
	@Override
	public void run() {
		while(true)
		{
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				return;
			}
			n++;
			repaint();
		}
	}
	//글자 가운데 출력 
	public void printFontCenter(Graphics2D g2, float height, String str, int size, Color color, Boolean bold) {
		Font font = new Font(Font.SANS_SERIF,Font.PLAIN,size);
		if(bold) {font = new Font(Font.SANS_SERIF,Font.BOLD,size); }
		FontMetrics metrics = g2.getFontMetrics(font);
		int font_width = metrics.stringWidth(str);
		g2.setFont(font);
		g2.setColor(color);
		g2.drawString(str, 375-font_width/2, height);
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		Dimension size = this.getSize();
		int w =size.width; 
		int h =size.height;
		GradientPaint gp = new GradientPaint(0,0,new Color(212,223,230),0,h,new Color(142,192,228));
		g2.setPaint(gp);
		g2.fillRect(0, 0, w, h);
	}
	@Override
	public void keyTyped(KeyEvent e) {
	}
	@Override
	public void keyPressed(KeyEvent e) {
		if(e.getKeyCode()==KeyEvent.VK_SPACE) {//스페이스바 눌렸을 경우 
			t.interrupt();
			switch(PanelName) {
				case "Opening":clip.stop(); hw5Panel.changeMode("Playing"); break;
				case "Playing": hw5Panel.changeMode("Ending"); break;
				case "Ending":clip.stop(); hw5Panel.changeMode("Opening"); break;
			}
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
	}
}

class Hw5PanelPlaying extends Hw5SubPanel{ // 게임 패널 
	LinkedList<Block> block =new LinkedList<Block>();//블록을 넣을 연결리스트 
	LinkedList<Ball> ball=new LinkedList<Ball>();//공을 넣을 연결리스트 
	int x = 340, y= 670;// 라켓 위치 
	int rWidth =140, rHeight = 25; //라켓 크기
	int bWidth, bHeight;//블록 크기 
	int score =0;
	int stage =1;
	
	int n=rWidth/5;//라켓 5분할
	int x1=x+n;
	int x2=x1+n;
	int x3=x2+n;
	int x4=x3+n;
	int x5=x4+n;
	
	Clip clipBreak;
	Clip clipLevel;
	Clip clipAttack;

	Hw5PanelPlaying(Hw5Panel hw5Panel){
		
		super(hw5Panel);
	    try {
	    	clipBreak = AudioSystem.getClip();
	    	clipLevel = AudioSystem.getClip();
	    	clipAttack = AudioSystem.getClip();
	    	URL urlBreak = getClass().getResource("break.wav");
	    	URL urlLevel = getClass().getResource("levelup.wav");
	    	URL urlAttack = getClass().getResource("attack.wav");
	    	AudioInputStream streamBreak  = AudioSystem.getAudioInputStream(urlBreak);
	    	AudioInputStream streamLevel  = AudioSystem.getAudioInputStream(urlLevel);
	    	AudioInputStream streamAttack  = AudioSystem.getAudioInputStream(urlAttack);
	    	clipBreak.open(streamBreak);
	    	clipLevel.open(streamLevel);
	    	clipAttack.open(streamAttack);
			
		} catch (Exception e) {
			return;
		}
	    
		PanelName="Playing";
		Ball b = new Ball();
		ball.add(b);
		setStage();
	}
	
	
	class Ball{ //Ball 클래스 
		   int bx=400;
		   int by=650;
		   int br=10;
		   int ballcenter=(bx+br/2); 
		   Point []pt=new Point[4]; //공의 상하좌우 네 점을 저장 
		   int dx=-1;
		   int dy=3;
		   Ball()
		   {
		      pt[0]=new Point(bx, by+(br/2)); // 좌 
		      pt[1]=new Point(bx+(br/2), by); // 상 
		      pt[2]=new Point(bx+br, by+(br/2)); // 우 
		      pt[3]=new Point(bx+(br/2), by+br); // 하 
		   }
		   Ball(int x, int y, int _dx, int _dy)
		   {
		      bx=x;
		      by=y;
		      ballcenter=(bx+br/2);
		      dx=_dx;
		      dy=_dy;
		      pt[0]=new Point(bx, by+(br/2));
		      pt[1]=new Point(bx+(br/2), by);
		      pt[2]=new Point(bx+br, by+(br/2));
		      pt[3]=new Point(bx+(br/2), by+br);
		   }
		}

	class Block{ // 벽돌 
			
			Point pt=new Point();
			boolean special=false;	
			Block(Point p, boolean spe)
			{
				pt=p;
				special=spe;
			}
		}
	
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g); // 배경그리기 
		Graphics2D g2 = (Graphics2D)g;
		for(Block bl : block) {
			if(bl.special) {
				int random =(int)((Math.random()*100)%7+1);
				GradientPaint gp1;
				if(random == 1) {
					gp1=new GradientPaint(bl.pt.x,bl.pt.y,new Color(255,255,255),bl.pt.x,bl.pt.y+bHeight,new Color(246,246,246));
				}
				else {
					gp1=new GradientPaint(bl.pt.x,bl.pt.y,new Color(230,255,255),bl.pt.x,bl.pt.y+bHeight,new Color(246,246,246));
		        }
				g2.setPaint(gp1);
			}
			else {
				GradientPaint gp1=new GradientPaint(bl.pt.x,bl.pt.y,new Color(106,132,185),bl.pt.x,bl.pt.y+bHeight,new Color(53,71,125));
	            g2.setPaint(gp1);
			}
			 g2.fillRoundRect(bl.pt.x, bl.pt.y, bWidth, bHeight, 5, 5);
		}//블록 그리기 
		setBall();
		g2.setColor(new Color(251,146,158));
		g2.fillRoundRect(x, y, rWidth, rHeight, 10, 10); // 라켓 그리기 
		g2.setColor(new Color(246,246,246));
		for(int i=0;i<ball.size();i++) {
			g2.fillOval(ball.get(i).bx, ball.get(i).by, ball.get(i).br, ball.get(i).br);
		}//공 그리기 
	}
	@Override
	public void keyPressed(KeyEvent e) {
		super.keyPressed(e);
		if(PanelName.equals("Playing")){
			if(e.getKeyCode()==KeyEvent.VK_LEFT) {
				if(x<70) x=0;
				else x-=60;
			}
			if(e.getKeyCode()==KeyEvent.VK_RIGHT) {
				if(x+140>630) x=610;
				else x+=60;
			}
		}
		divideRacket();
		setBall();
		repaint();
	}
	@Override
	public void run() {
		int interval=100;
		while(true) {
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				return;
			}
			switch(stage) {
				case 1:interval = 10; break;
				case 2:interval = 9; break;
				case 3:interval = 8; break;
				case 4:interval = 8; break;
			}
			for(int i=0; i<ball.size();i++) {
				ball.get(i).by-=ball.get(i).dy;
			}
			
			for(int i=0;i<ball.size();i++) {
				if(ball.get(i).by>800)
					ball.remove(i);
			}
			if(ball.size()<=0) {
				
				gameOver();
			}
			repaint();
		}
	}
	
	
	boolean check_collision(Point p, int _x, int _y, Point bp) {//충돌 검사
		Rectangle2D r=new Rectangle2D.Float(p.x,p.y,_x,_y);
	    if (r.contains(bp))
	    	return true;
	    else
	    	return false;
	}
	
	void resolve_collision() { // 벽돌 삭제 
		for(int k=0; k<ball.size();k++) {
			for(int i=0;i<4;i++) {
				for(int j=0;j<block.size();j++) {
					if(check_collision(block.get(j).pt,bWidth,bHeight,ball.get(k).pt[i])) {
						if (i==1 || i==3) // 상하가 충돌 
							ball.get(k).dy=-ball.get(k).dy; //y방향을 바꿔줌 
						else if (i==0 || i==2) // 좌우가 충돌 
							ball.get(k).dx=-ball.get(k).dx; //x방향을 바꿔줌
						 if (block.get(j).special==true){//공의 갯수 추가 	 
		                     Ball b1=new Ball(ball.get(k).bx, ball.get(k).by+10, ball.get(k).dx+1, ball.get(k).dy);
		                     Ball b2=new Ball(ball.get(k).bx, ball.get(k).by+10, ball.get(k).dx-1, ball.get(k).dy);
		                     ball.add(b1);
		                     ball.add(b2);
		                     clipAttack.setFramePosition(0);
			     			 clipAttack.start();
			                 
						 }
						 else {
			                 clipBreak.setFramePosition(0);
			     			 clipBreak.start();
						 }
						 score+=10;
		                 block.remove(j); //충돌한 벽돌 삭제 

					}
				}
			}
		} 
	}
	
	void divideRacket() { //라켓을 5부분으로 나눔 
		n=rWidth/5;
		x1=x+n;
		x2=x1+n;
		x3=x2+n;
		x4=x3+n;
		x5=x4+n;
	}
	
	void setBall() { //공의 속도, 방향 바꿈 
		divideRacket();
		for (int i=0; i<ball.size(); i++) {
			ball.get(i).ballcenter=(ball.get(i).bx+(ball.get(i).br/2));
		}
		for (int i=0; i<ball.size(); i++){
			if(ball.size()<=0) break;
			if (ball.get(i).by+ball.get(i).br>=670 && ball.get(i).by+ball.get(i).br <= 695 && ball.get(i).ballcenter>=x && ball.get(i).ballcenter<=x+rWidth){
				//라켓과 공이 부딪혔을 때 
				ball.get(i).by=650;
				ball.get(i).dy=-ball.get(i).dy;
	            if ( x<=ball.get(i).ballcenter && x1>=ball.get(i).ballcenter)
	            	ball.get(i).dx=-3;
	            else if (x1<ball.get(i).ballcenter && ball.get(i).ballcenter<=x2)
	            	ball.get(i).dx=-2;
	            else if (x2<ball.get(i).ballcenter && ball.get(i).ballcenter<=x3)
	            	 ball.get(i).dx=1;
	            else if (x3<ball.get(i).ballcenter && ball.get(i).ballcenter<=x4)
	            	 ball.get(i).dx=2;
	            else if (x4<ball.get(i).ballcenter && ball.get(i).ballcenter<=x5)
	            	 ball.get(i).dx=3;
			}
		}
		for (int i=0; i<ball.size(); i++) {
			if (ball.get(i).bx<0 || ball.get(i).bx>=750-ball.get(i).br) // 벽에 공이 부딪힌 경우 
				ball.get(i).dx=-ball.get(i).dx;
	        if (ball.get(i).by<20) { // 위에 부딪힌 경우 
	            ball.get(i).by=20;
	            ball.get(i).dy=-ball.get(i).dy;
	        }
	    }// 공의 방향 다시 설정 
		for (int i=0; i<ball.size(); i++){
			ball.get(i).bx+=ball.get(i).dx;
			ball.get(i).pt[0]=new Point(ball.get(i).bx, ball.get(i).by+(ball.get(i).br/2));
			ball.get(i).pt[1]=new Point(ball.get(i).bx+(ball.get(i).br/2), ball.get(i).by);
			ball.get(i).pt[2]=new Point(ball.get(i).bx+ball.get(i).br, ball.get(i).by+(ball.get(i).br/2));
			ball.get(i).pt[3]=new Point(ball.get(i).bx+(ball.get(i).br/2), ball.get(i).by+ball.get(i).br);
		}// 공의 상하좌우 점 다시 설정 
		resolve_collision(); //충돌한 블록 삭제 
		if(block.size()<=0) { //블록이 모두 삭제 되었을 경우
			stage++;
			setStage();
            clipLevel.setFramePosition(0);
			clipLevel.start();
		}
	}
	
	void setBlock(int width, int height) { //스테이지 별로 블록 셋팅 
		int random; 
		bWidth=width;
		bHeight=height;
		for(int i=0;i<3*stage;i++) { //블록 갯수 = (스테이지*3)^2
			for(int j=0;j<3*stage;j++) {
				Point p = new Point (10+(width*j)+(5*j),25+(5*i)+(height*i));
				boolean temp = false;
				random=(int)((Math.random()*10)%5+1);
				if(random == 1) {
					temp = true;
				}
				block.add(new Block(p,temp));
			}
		}
	}
	void setStage() { // 공 새로만들고 블록 셋팅 
		ball.removeAll(ball);
		Ball b = new Ball();
		b.bx=x+50;
		b.by=640;
		ball.add(b);
		if(stage==1) {
			setBlock(240,90); //블록 하나의 가로, 세로 높이 인자로 전달 
		}
		else if(stage==2) {
			setBlock(117,50);
		}
		else if(stage==3) {
			setBlock(76,40);
		}
		else if(stage==4) {
			setBlock(56,30);
		}
		else {
			gameOver();
		}
	}


	void gameOver() {
		if(score>(hw5Panel.getScore())) {
			hw5Panel.setScore(score); //최고 점수 비교 후 수정 
		}
		hw5Panel.myScore = score;
		t.interrupt();
		hw5Panel.changeMode("Ending");
	}
}


class Hw5PanelEnding extends Hw5SubPanel{ // 서브패널을 상속 받는 종료패널 
	Hw5PanelEnding(Hw5Panel hw5Panel) {
		super(hw5Panel);
		PanelName="Ending";
		try {
			clip = AudioSystem.getClip();
	    	URL url = getClass().getResource("gameover.wav");
	    	AudioInputStream stream = AudioSystem.getAudioInputStream(url);
	    	clip.open(stream);
	    	clip.setFramePosition(0);
	    	clip.start();
		} catch (Exception e) {
		return;
		}
	}
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		printFontCenter(g2, 300, "Game Over",90,new Color(255,255,255),true);
		printFontCenter(g2, 420, "High Score: "+hw5Panel.getScore(),40,new Color(155,155,155),false);
		printFontCenter(g2, 470, "Your Score: "+hw5Panel.myScore,40,new Color(155,155,155),false);
		if(n%2==0) {
		printFontCenter(g2, 600, "Press SpaceBar to Play!",30,new Color(3,0,65),false);
		}
	}
}


class Hw5PanelOpening extends Hw5SubPanel{ // 서브패널 상속 받는 시작패널 
	
	Hw5PanelOpening(Hw5Panel hw5Panel) {
		super(hw5Panel);
		PanelName="Opening";
		try {
			clip = AudioSystem.getClip();
	    	URL url = getClass().getResource("opening.wav");
	    	AudioInputStream stream = AudioSystem.getAudioInputStream(url);
	    	clip.open(stream);
	    	clip.setFramePosition(0);
	    	clip.start();
		} catch (Exception e) {
		return;
		}
    	
	}		

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		printFontCenter(g2, 100, "Java Programming",50,new Color(255,255,255),false);
		printFontCenter(g2, 200, "Homework #5",50,new  Color(255,255,255),false);
		printFontCenter(g2, 400, "Block Breaker",90,new Color(255,255,255),true);
		if(n%2==0) {
		printFontCenter(g2, 600, "Press SpaceBar to Play!",30,new Color(3,0,65),false);
		}
	}	
}

public class Hw5 extends JFrame{
	
	Hw5(){
		setSize(800,800);
		setTitle("Java Homework5");
		add(new Hw5Panel());
		setResizable(false);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	public static void main(String[] args) {
		//LookAndFeel변경 
		try {	
			UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
		 } catch (Exception e) {
			 e.printStackTrace();
		 }		
		new Hw5();
	}
}
