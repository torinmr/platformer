import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends Canvas {
    BufferStrategy bf;
    int xSize, ySize;
    int xRes, yRes;
    Tiles tiles;
    InputListener input;
    Player player;
    Editor editor;
    BufferedImage render;
    Camera camera;

    public Game(GraphicsDevice device) {
        this.xSize = 80*32;
        this.ySize = 50*32;
        this.xRes = 25*32;
        this.yRes = 18*32;
        
        JFrame container = new JFrame("Game");
        container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = (JPanel) container.getContentPane();
        panel.setPreferredSize(new Dimension(xRes, yRes));
        panel.setLayout(null);
        
        setBounds(0,0,xRes,yRes);
        panel.add(this);
        setIgnoreRepaint(true);        

        container.pack();
        container.setResizable(false);
        container.setVisible(true);
        
        createBufferStrategy(2);            
        bf = getBufferStrategy();

        // Set up the render. We draw to the render, which is then clipped
        // by the camera and drawn to the screen
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        render = gc.createCompatibleImage(xSize, ySize,
                                          Transparency.BITMASK);
        
        camera = new Camera(xRes, yRes, 0, 0, Camera.SMART);

        // Set up game world
        tiles = new Tiles(50, 36, 32);
        for (int i = 0; i < tiles.getWidth(); i++) {
            for (int j = 0; j < tiles.getWidth(); j++) {
                if (i == 0 || i == tiles.getWidth() - 1 
                    || j == 0 || j == tiles.getHeight() - 1) {
                    tiles.setTile(i, j, "./assets/tiles/block.png", true);
                } else {
                    tiles.setTile(i, j, "./assets/tiles/background.png", false);
                }
            }
        }

        player = new Player(tiles, 50,50);

        // Set up input listeners
        input = new InputListener(this);
        
        // Set up the world editor
        editor = new Editor(tiles);
    }

    public void handleInput(long delta) {
        editor.handleInput(input, camera);
        player.handleInput(input, delta);
    }

    public void update(long delta) {
        player.updatePhysics(delta);
        player.move(delta);
        camera.update(player, tiles, delta);
    }

    public void draw() {
        Graphics gRender = render.getGraphics();
        tiles.draw(gRender);
        player.draw(gRender);
        
        Graphics g = null;
        
        try {
            g = bf.getDrawGraphics();
            camera.render(g, render);
        } finally {
            g.dispose();
        }

        bf.show();        
    }

    static Game game;

    public static void init() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device = env.getDefaultScreenDevice();
        game = new Game(device);

        Thread loop = new Thread()
            {
                public void run()
                {
                    loop();
                }
            };
        loop.start();
    }

    public static void loop() {
        long millisecondsPerFrame = 10;
        long beginningTime;
        long endTime;
        long lastFrameTime = millisecondsPerFrame;
        for ( ; ; ) {
            beginningTime = System.currentTimeMillis();
            
            game.handleInput(lastFrameTime);
            game.update(lastFrameTime);
            game.draw();
            
            
            while (System.currentTimeMillis() - beginningTime < millisecondsPerFrame)
                {
                }
            
            lastFrameTime = System.currentTimeMillis() - beginningTime; 
            
            StdOut.printf("FPS = %d\n", 1000 / lastFrameTime);
 
        }
    }

    public static void main(String args[]) {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    init();
                }
            });
    }
}