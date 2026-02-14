// Builder Pattern Example - 分步骤构建复杂对象

// 产品类
class Computer {
    // 必需属性
    private String cpu;
    private String ram;
    
    // 可选属性
    private String storage;
    private String graphicsCard;
    private String coolingSystem;
    private String motherboard;
    
    // 私有构造函数
    private Computer(Builder builder) {
        this.cpu = builder.cpu;
        this.ram = builder.ram;
        this.storage = builder.storage;
        this.graphicsCard = builder.graphicsCard;
        this.coolingSystem = builder.coolingSystem;
        this.motherboard = builder.motherboard;
    }
    
    @Override
    public String toString() {
        return "Computer配置:\n" +
               "  CPU: " + cpu + "\n" +
               "  RAM: " + ram + "\n" +
               "  存储: " + (storage != null ? storage : "未配置") + "\n" +
               "  显卡: " + (graphicsCard != null ? graphicsCard : "未配置") + "\n" +
               "  散热: " + (coolingSystem != null ? coolingSystem : "未配置") + "\n" +
               "  主板: " + (motherboard != null ? motherboard : "未配置");
    }
    
    // 静态建造者类
    public static class Builder {
        // 必需属性
        private String cpu;
        private String ram;
        
        // 可选属性
        private String storage;
        private String graphicsCard;
        private String coolingSystem;
        private String motherboard;
        
        // 构造函数只包含必需参数
        public Builder(String cpu, String ram) {
            this.cpu = cpu;
            this.ram = ram;
        }
        
        // 设置可选属性的方法，返回Builder本身支持链式调用
        public Builder setStorage(String storage) {
            this.storage = storage;
            return this;
        }
        
        public Builder setGraphicsCard(String graphicsCard) {
            this.graphicsCard = graphicsCard;
            return this;
        }
        
        public Builder setCoolingSystem(String coolingSystem) {
            this.coolingSystem = coolingSystem;
            return this;
        }
        
        public Builder setMotherboard(String motherboard) {
            this.motherboard = motherboard;
            return this;
        }
        
        // 构建最终产品
        public Computer build() {
            return new Computer(this);
        }
    }
}

// 另一个建造者示例 - 房屋建造
class House {
    private String foundation;
    private String structure;
    private String roof;
    private boolean hasGarage;
    private boolean hasSwimmingPool;
    private String interiorDesign;
    
    private House(HouseBuilder builder) {
        this.foundation = builder.foundation;
        this.structure = builder.structure;
        this.roof = builder.roof;
        this.hasGarage = builder.hasGarage;
        this.hasSwimmingPool = builder.hasSwimmingPool;
        this.interiorDesign = builder.interiorDesign;
    }
    
    @Override
    public String toString() {
        return "房屋配置:\n" +
               "  地基: " + foundation + "\n" +
               "  结构: " + structure + "\n" +
               "  屋顶: " + roof + "\n" +
               "  车库: " + (hasGarage ? "有" : "无") + "\n" +
               "  游泳池: " + (hasSwimmingPool ? "有" : "无") + "\n" +
               "  室内设计: " + (interiorDesign != null ? interiorDesign : "标准装修");
    }
    
    public static class HouseBuilder {
        private String foundation;
        private String structure;
        private String roof;
        private boolean hasGarage = false;
        private boolean hasSwimmingPool = false;
        private String interiorDesign;
        
        public HouseBuilder setFoundation(String foundation) {
            this.foundation = foundation;
            return this;
        }
        
        public HouseBuilder setStructure(String structure) {
            this.structure = structure;
            return this;
        }
        
        public HouseBuilder setRoof(String roof) {
            this.roof = roof;
            return this;
        }
        
        public HouseBuilder setHasGarage(boolean hasGarage) {
            this.hasGarage = hasGarage;
            return this;
        }
        
        public HouseBuilder setHasSwimmingPool(boolean hasSwimmingPool) {
            this.hasSwimmingPool = hasSwimmingPool;
            return this;
        }
        
        public HouseBuilder setInteriorDesign(String interiorDesign) {
            this.interiorDesign = interiorDesign;
            return this;
        }
        
        public House build() {
            return new House(this);
        }
    }
}

// 建造者接口示例
interface MealBuilder {
    void buildBurger();
    void buildDrink();
    void buildDessert();
    Meal getMeal();
}

class Meal {
    private String burger;
    private String drink;
    private String dessert;
    
    public void setBurger(String burger) {
        this.burger = burger;
    }
    
    public void setDrink(String drink) {
        this.drink = drink;
    }
    
    public void setDessert(String dessert) {
        this.dessert = dessert;
    }
    
    @Override
    public String toString() {
        return "套餐内容:\n" +
               "  汉堡: " + (burger != null ? burger : "无") + "\n" +
               "  饮料: " + (drink != null ? drink : "无") + "\n" +
               "  甜点: " + (dessert != null ? dessert : "无");
    }
}

class VegMealBuilder implements MealBuilder {
    private Meal meal;
    
    public VegMealBuilder() {
        this.meal = new Meal();
    }
    
    @Override
    public void buildBurger() {
        meal.setBurger("素食汉堡");
    }
    
    @Override
    public void buildDrink() {
        meal.setDrink("橙汁");
    }
    
    @Override
    public void buildDessert() {
        meal.setDessert("水果沙拉");
    }
    
    @Override
    public Meal getMeal() {
        return meal;
    }
}

class NonVegMealBuilder implements MealBuilder {
    private Meal meal;
    
    public NonVegMealBuilder() {
        this.meal = new Meal();
    }
    
    @Override
    public void buildBurger() {
        meal.setBurger("牛肉汉堡");
    }
    
    @Override
    public void buildDrink() {
        meal.setDrink("可乐");
    }
    
    @Override
    public void buildDessert() {
        meal.setDessert("冰淇淋");
    }
    
    @Override
    public Meal getMeal() {
        return meal;
    }
}

// 指导者类
class MealDirector {
    private MealBuilder mealBuilder;
    
    public MealDirector(MealBuilder mealBuilder) {
        this.mealBuilder = mealBuilder;
    }
    
    public Meal constructMeal() {
        mealBuilder.buildBurger();
        mealBuilder.buildDrink();
        mealBuilder.buildDessert();
        return mealBuilder.getMeal();
    }
    
    public Meal constructVegetarianMeal() {
        mealBuilder.buildBurger();
        mealBuilder.buildDrink();
        return mealBuilder.getMeal();
    }
}

public class BuilderPatternExample {
    public static void main(String[] args) {
        System.out.println("=== 建造者模式示例 ===\n");
        
        // 计算机建造示例
        System.out.println("1. 计算机建造示例:");
        
        // 基础配置电脑
        Computer basicComputer = new Computer.Builder("Intel i5", "8GB")
                                    .build();
        System.out.println(basicComputer);
        
        System.out.println();
        
        // 高端游戏电脑
        Computer gamingComputer = new Computer.Builder("AMD Ryzen 9", "32GB")
                                    .setStorage("1TB SSD")
                                    .setGraphicsCard("RTX 4080")
                                    .setCoolingSystem("水冷散热")
                                    .setMotherboard("高端主板")
                                    .build();
        System.out.println(gamingComputer);
        
        System.out.println();
        
        // 办公电脑
        Computer officeComputer = new Computer.Builder("Intel i7", "16GB")
                                    .setStorage("512GB SSD")
                                    .setGraphicsCard("集成显卡")
                                    .build();
        System.out.println(officeComputer);
        
        // 房屋建造示例
        System.out.println("\n2. 房屋建造示例:");
        
        House luxuryHouse = new House.HouseBuilder()
                                .setFoundation("混凝土地基")
                                .setStructure("钢筋混凝土结构")
                                .setRoof("瓦片屋顶")
                                .setHasGarage(true)
                                .setHasSwimmingPool(true)
                                .setInteriorDesign("豪华装修")
                                .build();
        System.out.println(luxuryHouse);
        
        System.out.println();
        
        House simpleHouse = new House.HouseBuilder()
                                .setFoundation("砖石地基")
                                .setStructure("砖混结构")
                                .setRoof("铁皮屋顶")
                                .build();
        System.out.println(simpleHouse);
        
        // 套餐建造示例
        System.out.println("\n3. 套餐建造示例:");
        
        // 素食套餐
        MealBuilder vegBuilder = new VegMealBuilder();
        MealDirector director = new MealDirector(vegBuilder);
        Meal vegMeal = director.constructMeal();
        System.out.println(vegMeal);
        
        System.out.println();
        
        // 非素食套餐
        MealBuilder nonVegBuilder = new NonVegMealBuilder();
        MealDirector director2 = new MealDirector(nonVegBuilder);
        Meal nonVegMeal = director2.constructVegetarianMeal(); // 只要汉堡和饮料
        System.out.println(nonVegMeal);
        
        // 建造者模式优势演示
        System.out.println("\n4. 建造者模式优势:");
        demonstrateBuilderAdvantages();
    }
    
    private static void demonstrateBuilderAdvantages() {
        System.out.println("建造者模式的主要优势:");
        System.out.println("1. 将复杂对象的构建与其表示分离");
        System.out.println("2. 支持链式调用，代码更易读");
        System.out.println("3. 可以控制构建过程的步骤");
        System.out.println("4. 同一构建过程可以创建不同的表示");
        System.out.println("5. 避免了构造函数参数过多的问题");
        System.out.println("6. 符合单一职责原则");
    }
}