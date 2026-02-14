// Adapter Pattern Example - 让不兼容的接口能够协同工作

// 目标接口 - 客户端期望的接口
interface MediaPlayer {
    void play(String audioType, String fileName);
}

// 被适配者 - 已存在的具体实现
class Mp3Player {
    public void playMp3(String fileName) {
        System.out.println("播放MP3文件: " + fileName);
    }
}

class Mp4Player {
    public void playMp4(String fileName) {
        System.out.println("播放MP4文件: " + fileName);
    }
}

class VlcPlayer {
    public void playVlc(String fileName) {
        System.out.println("播放VLC文件: " + fileName);
    }
}

// 类适配器 - 通过继承实现
class ClassAdapter extends Mp3Player implements MediaPlayer {
    @Override
    public void play(String audioType, String fileName) {
        if ("mp3".equalsIgnoreCase(audioType)) {
            playMp3(fileName);
        } else {
            System.out.println("类适配器不支持格式: " + audioType);
        }
    }
}

// 对象适配器 - 通过组合实现（推荐方式）
class ObjectAdapter implements MediaPlayer {
    private Mp3Player mp3Player;
    private Mp4Player mp4Player;
    private VlcPlayer vlcPlayer;
    
    public ObjectAdapter() {
        this.mp3Player = new Mp3Player();
        this.mp4Player = new Mp4Player();
        this.vlcPlayer = new VlcPlayer();
    }
    
    @Override
    public void play(String audioType, String fileName) {
        if ("mp3".equalsIgnoreCase(audioType)) {
            mp3Player.playMp3(fileName);
        } else if ("mp4".equalsIgnoreCase(audioType)) {
            mp4Player.playMp4(fileName);
        } else if ("vlc".equalsIgnoreCase(audioType)) {
            vlcPlayer.playVlc(fileName);
        } else {
            System.out.println("对象适配器不支持格式: " + audioType);
        }
    }
}

// 双向适配器示例
interface EuropeanSocket {
    void provideElectricity();
}

interface USASocket {
    void supplyPower();
}

class EuropeanSocketImpl implements EuropeanSocket {
    @Override
    public void provideElectricity() {
        System.out.println("欧洲插座提供220V电压");
    }
}

class USASocketImpl implements USASocket {
    @Override
    public void supplyPower() {
        System.out.println("美国插座提供110V电压");
    }
}

// 双向适配器
class SocketAdapter implements EuropeanSocket, USASocket {
    private EuropeanSocket europeanSocket;
    private USASocket usaSocket;
    
    public SocketAdapter(EuropeanSocket europeanSocket) {
        this.europeanSocket = europeanSocket;
    }
    
    public SocketAdapter(USASocket usaSocket) {
        this.usaSocket = usaSocket;
    }
    
    @Override
    public void provideElectricity() {
        if (usaSocket != null) {
            System.out.print("适配器转换: ");
            usaSocket.supplyPower();
            System.out.println("  -> 转换为欧洲标准220V");
        } else {
            europeanSocket.provideElectricity();
        }
    }
    
    @Override
    public void supplyPower() {
        if (europeanSocket != null) {
            System.out.print("适配器转换: ");
            europeanSocket.provideElectricity();
            System.out.println("  -> 转换为美国标准110V");
        } else {
            usaSocket.supplyPower();
        }
    }
}

// 接口适配器示例 - 为接口提供默认实现
interface AdvancedMediaPlayer {
    void playMp3(String fileName);
    void playMp4(String fileName);
    void playVlc(String fileName);
    void playAvi(String fileName);
}

// 抽象适配器提供默认实现
abstract class MediaAdapter implements AdvancedMediaPlayer {
    @Override
    public void playMp3(String fileName) {
        // 默认不支持
        System.out.println("默认适配器不支持MP3格式");
    }
    
    @Override
    public void playMp4(String fileName) {
        // 默认不支持
        System.out.println("默认适配器不支持MP4格式");
    }
    
    @Override
    public void playVlc(String fileName) {
        // 默认不支持
        System.out.println("默认适配器不支持VLC格式");
    }
    
    @Override
    public void playAvi(String fileName) {
        // 默认不支持
        System.out.println("默认适配器不支持AVI格式");
    }
}

// 具体适配器只需实现需要的方法
class CustomMediaAdapter extends MediaAdapter {
    @Override
    public void playMp3(String fileName) {
        System.out.println("自定义适配器播放MP3: " + fileName);
    }
    
    @Override
    public void playMp4(String fileName) {
        System.out.println("自定义适配器播放MP4: " + fileName);
    }
}

public class AdapterPatternExample {
    public static void main(String[] args) {
        System.out.println("=== 适配器模式示例 ===\n");
        
        // 媒体播放器适配器示例
        System.out.println("1. 媒体播放器适配器示例:");
        
        // 使用类适配器
        System.out.println("--- 类适配器 ---");
        MediaPlayer classMediaPlayer = new ClassAdapter();
        classMediaPlayer.play("mp3", "song.mp3");
        classMediaPlayer.play("mp4", "video.mp4"); // 不支持
        
        System.out.println();
        
        // 使用对象适配器
        System.out.println("--- 对象适配器 ---");
        MediaPlayer objectMediaPlayer = new ObjectAdapter();
        objectMediaPlayer.play("mp3", "music.mp3");
        objectMediaPlayer.play("mp4", "movie.mp4");
        objectMediaPlayer.play("vlc", "documentary.vlc");
        objectMediaPlayer.play("avi", "old_movie.avi"); // 不支持
        
        // 插座适配器示例
        System.out.println("\n2. 插座适配器示例:");
        
        EuropeanSocket euroSocket = new EuropeanSocketImpl();
        USASocket usaSocket = new USASocketImpl();
        
        // 欧洲设备使用美国插座
        System.out.println("--- 欧洲设备使用美国插座 ---");
        SocketAdapter euroToUsa = new SocketAdapter(usaSocket);
        euroToUsa.provideElectricity(); // 适配为欧洲标准
        
        // 美国设备使用欧洲插座
        System.out.println("--- 美国设备使用欧洲插座 ---");
        SocketAdapter usaToEuro = new SocketAdapter(euroSocket);
        usaToEuro.supplyPower(); // 适配为美国标准
        
        // 接口适配器示例
        System.out.println("\n3. 接口适配器示例:");
        AdvancedMediaPlayer customPlayer = new CustomMediaAdapter();
        customPlayer.playMp3("custom_song.mp3");
        customPlayer.playMp4("custom_video.mp4");
        customPlayer.playVlc("custom_documentary.vlc"); // 使用默认实现
        customPlayer.playAvi("custom_movie.avi"); // 使用默认实现
        
        // 实际应用场景示例
        System.out.println("\n4. 实际应用场景:");
        demonstrateRealWorldUsage();
    }
    
    private static void demonstrateRealWorldUsage() {
        System.out.println("适配器模式常见应用场景:");
        System.out.println("1. 集成第三方库或遗留系统");
        System.out.println("2. 不同数据库驱动的统一接口");
        System.out.println("3. 不同支付网关的统一封装");
        System.out.println("4. 不同日志框架的适配");
        System.out.println("5. 硬件设备驱动程序");
        System.out.println("6. API版本兼容性处理");
        System.out.println("7. 不同数据格式间的转换");
        
        System.out.println("\n适配器模式的两种实现方式:");
        System.out.println("- 类适配器: 通过继承实现 (Java不支持多继承，有限制)");
        System.out.println("- 对象适配器: 通过组合实现 (推荐，更灵活)");
    }
}