// Singleton Pattern Example - 确保一个类只有一个实例，并提供全局访问点

// 饿汉式单例 - 线程安全，类加载时就创建实例
class EagerSingleton {
    // 在类加载时就创建实例
    private static final EagerSingleton INSTANCE = new EagerSingleton();
    
    // 私有构造函数，防止外部实例化
    private EagerSingleton() {
        System.out.println("饿汉式单例被创建");
    }
    
    // 提供全局访问点
    public static EagerSingleton getInstance() {
        return INSTANCE;
    }
    
    public void showMessage() {
        System.out.println("这是饿汉式单例");
    }
}

// 懒汉式单例 - 延迟加载，但需要处理线程安全问题
class LazySingleton {
    private static LazySingleton instance;
    
    private LazySingleton() {
        System.out.println("懒汉式单例被创建");
    }
    
    // 线程不安全的版本
    public static LazySingleton getInstanceUnsafe() {
        if (instance == null) {
            instance = new LazySingleton();
        }
        return instance;
    }
    
    // 线程安全但性能较差的版本
    public static synchronized LazySingleton getInstanceSafe() {
        if (instance == null) {
            instance = new LazySingleton();
        }
        return instance;
    }
    
    // 双重检查锁定 - 推荐的线程安全懒加载方式
    public static LazySingleton getInstanceDoubleCheck() {
        if (instance == null) {
            synchronized (LazySingleton.class) {
                if (instance == null) {
                    instance = new LazySingleton();
                }
            }
        }
        return instance;
    }
    
    public void showMessage() {
        System.out.println("这是懒汉式单例");
    }
}

// 静态内部类单例 - 既保证线程安全又实现延迟加载
class StaticInnerClassSingleton {
    private StaticInnerClassSingleton() {
        System.out.println("静态内部类单例被创建");
    }
    
    // 静态内部类
    private static class SingletonHolder {
        private static final StaticInnerClassSingleton INSTANCE = new StaticInnerClassSingleton();
    }
    
    public static StaticInnerClassSingleton getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    public void showMessage() {
        System.out.println("这是静态内部类单例");
    }
}

// 枚举单例 - 最简单的线程安全单例实现
enum EnumSingleton {
    INSTANCE;
    
    public void showMessage() {
        System.out.println("这是枚举单例");
    }
}

public class SingletonPatternExample {
    public static void main(String[] args) {
        System.out.println("=== 单例模式示例 ===\n");
        
        // 测试饿汉式单例
        System.out.println("1. 饿汉式单例:");
        EagerSingleton eager1 = EagerSingleton.getInstance();
        EagerSingleton eager2 = EagerSingleton.getInstance();
        eager1.showMessage();
        System.out.println("两个实例是否相同: " + (eager1 == eager2));
        
        System.out.println("\n2. 懒汉式单例:");
        LazySingleton lazy1 = LazySingleton.getInstanceDoubleCheck();
        LazySingleton lazy2 = LazySingleton.getInstanceDoubleCheck();
        lazy1.showMessage();
        System.out.println("两个实例是否相同: " + (lazy1 == lazy2));
        
        System.out.println("\n3. 静态内部类单例:");
        StaticInnerClassSingleton static1 = StaticInnerClassSingleton.getInstance();
        StaticInnerClassSingleton static2 = StaticInnerClassSingleton.getInstance();
        static1.showMessage();
        System.out.println("两个实例是否相同: " + (static1 == static2));
        
        System.out.println("\n4. 枚举单例:");
        EnumSingleton enum1 = EnumSingleton.INSTANCE;
        EnumSingleton enum2 = EnumSingleton.INSTANCE;
        enum1.showMessage();
        System.out.println("两个实例是否相同: " + (enum1 == enum2));
        
        System.out.println("\n=== 性能对比测试 ===");
        // 性能测试
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            EagerSingleton.getInstance();
        }
        System.out.println("饿汉式100万次获取耗时: " + (System.currentTimeMillis() - startTime) + "ms");
        
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            LazySingleton.getInstanceDoubleCheck();
        }
        System.out.println("双重检查懒汉式100万次获取耗时: " + (System.currentTimeMillis() - startTime) + "ms");
    }
}