package com.example.caloriecalculator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * FoodDatabase
 * ------------
 * The app's built-in food list (works offline).
 *
 * Values are estimates. Sources:
 *  - Per-serving values for Malaysian dishes and drinks: Calculator Malaysia food calorie list
 *    (based on the MOH Malaysia food calorie bank, MyFCD, PersonalTraining.com.my and Gleneagles).
 *  - Roti canai (95 g = 301 kcal): Contours Express Malaysia food calorie list.
 *  - Per-100 g values for basic foods, fruits and fast food: USDA FoodData Central.
 *  - Items marked "estimate" are general estimates based on similar foods.
 * Serving weights are typical portion sizes used to convert per-serving values to per 100 g.
 *
 * The last values in each line are the names the AI food model uses for that dish.
 */
public class FoodDatabase {

    public static final String ALL = "All";
    public static final String RICE = "Rice";
    public static final String NOODLES = "Noodles";
    public static final String BREAD = "Bread & Breakfast";
    public static final String MEAT = "Meat & Sides";
    public static final String WESTERN = "Western";
    public static final String SNACKS = "Snacks & Desserts";
    public static final String FRUITS = "Fruits";
    public static final String DRINKS = "Drinks";

    public static final String[] CATEGORIES = {
            ALL, RICE, NOODLES, BREAD, MEAT, WESTERN, SNACKS, FRUITS, DRINKS
    };

    private static final String G = "g";
    private static final String ML = "ml";

    private static final List<FoodItem> FOODS = Arrays.asList(
            // ================= RICE =================
            FoodItem.fromServing("Nasi Lemak (plain)", RICE, G, 250, "1 pack", 400, "Nasi lemak", "Coconut rice"),
            FoodItem.fromServing("Nasi Lemak Ayam", RICE, G, 350, "1 plate", 650, "Nasi lemak"),
            FoodItem.fromServing("Nasi Goreng", RICE, G, 350, "1 plate", 600,
                    "Fried rice", "Yangzhou fried rice", "American fried rice", "Thai fried rice", "Kimchi fried rice"),
            FoodItem.fromServing("Chicken Rice (Nasi Ayam)", RICE, G, 350, "1 plate", 620,
                    "Hainanese chicken rice", "Claypot chicken rice", "White cut chicken", "Soy sauce chicken", "Hainanese curry rice"),
            FoodItem.fromServing("Nasi Kandar / Nasi Campur", RICE, G, 450, "1 plate", 800,
                    "Nasi campur", "Nasi tumpang", "Nasi bogana", "Nasi liwet"),
            FoodItem.fromServing("Nasi Briyani Ayam", RICE, G, 400, "1 plate", 700, "Hyderabadi biryani"),
            FoodItem.fromServing("Nasi Kerabu", RICE, G, 300, "1 plate", 400),
            FoodItem.fromServing("White Rice", RICE, G, 100, "1 scoop", 130, "White rice"),
            FoodItem.fromPer100("Rice Porridge (Bubur)", RICE, G, 50, 250, "1 bowl", "Congee", "Teochew porridge"),      // estimate
            FoodItem.fromPer100("Ketupat / Nasi Impit", RICE, G, 140, 100, "4 pieces", "Ketupat", "Lontong"),            // estimate
            FoodItem.fromPer100("Lemang", RICE, G, 200, 100, "2 pieces", "Lemang"),                                      // estimate

            // ================= NOODLES =================
            FoodItem.fromServing("Char Kuey Teow", NOODLES, G, 350, "1 plate", 745, "Char kway teow"),
            FoodItem.fromServing("Mee Goreng Mamak", NOODLES, G, 350, "1 plate", 660,
                    "Chinese noodles", "Lo mein", "Shanghai fried noodles", "Hot dry noodles"),
            FoodItem.fromServing("Bihun Goreng", NOODLES, G, 300, "1 plate", 430),
            FoodItem.fromServing("Maggi Goreng", NOODLES, G, 250, "1 plate", 400),
            FoodItem.fromServing("Asam Laksa", NOODLES, G, 500, "1 bowl", 400),
            FoodItem.fromServing("Mee Rebus", NOODLES, G, 450, "1 bowl", 480),
            FoodItem.fromServing("Wantan Mee (dry)", NOODLES, G, 300, "1 plate", 410, "Wonton noodles", "Mee pok", "Wonton"),
            FoodItem.fromServing("Noodle Soup (Bihun / Mee Sup)", NOODLES, G, 450, "1 bowl", 250, "Beef noodle soup"),
            FoodItem.fromPer100("Mee Siam", NOODLES, G, 150, 300, "1 plate", "Mee siam"),                                // estimate
            FoodItem.fromPer100("Soto Ayam", NOODLES, G, 60, 400, "1 bowl", "Soto ayam", "Soto", "Soto mie"),            // estimate
            FoodItem.fromPer100("Tom Yum Soup", NOODLES, G, 40, 300, "1 bowl", "Tom yum"),                               // estimate
            FoodItem.fromPer100("Spaghetti Bolognese", NOODLES, G, 130, 300, "1 plate",
                    "Spaghetti", "Spaghetti aglio e olio", "Spaghetti alle vongole"),                                     // estimate

            // ================= BREAD & BREAKFAST =================
            FoodItem.fromServing("Roti Canai (plain)", BREAD, G, 95, "1 piece", 301, "Mughlai paratha", "Aloo paratha"),
            FoodItem.fromServing("Roti Telur", BREAD, G, 130, "1 piece", 400, "Mughlai paratha"),
            FoodItem.fromPer100("Murtabak", BREAD, G, 230, 250, "1 piece", "Murtabak"),                                  // estimate
            FoodItem.fromPer100("Roti Jala", BREAD, G, 200, 100, "3 pieces", "Roti jala"),                               // estimate
            FoodItem.fromServing("Roti John", BREAD, G, 250, "1 piece", 470, "Submarine sandwich"),
            FoodItem.fromServing("Chapati", BREAD, G, 60, "1 piece", 150),
            FoodItem.fromServing("Thosai (plain)", BREAD, G, 100, "1 piece", 150),
            FoodItem.fromPer100("Kaya Toast", BREAD, G, 330, 70, "2 slices", "Kaya toast"),                              // estimate
            FoodItem.fromServing("White Bread", BREAD, G, 28, "1 slice", 75, "Sandwich loaf", "Milk toast"),
            FoodItem.fromServing("Boiled Egg", BREAD, G, 50, "1 egg", 78, "Tea egg"),
            FoodItem.fromServing("Fried Egg", BREAD, G, 46, "1 egg", 90),
            FoodItem.fromPer100("Omelette", BREAD, G, 154, 100, "1 omelette", "Omelette", "Oyster omelette"),
            FoodItem.fromServing("Oatmeal (plain)", BREAD, G, 250, "1 bowl", 150),
            FoodItem.fromPer100("Pancake", BREAD, G, 227, 80, "2 pieces", "Pancake"),
            FoodItem.fromPer100("Waffle", BREAD, G, 290, 75, "1 waffle", "Waffle"),
            FoodItem.fromPer100("French Toast", BREAD, G, 229, 65, "1 slice", "French toast"),
            FoodItem.fromPer100("Sandwich", BREAD, G, 250, 150, "1 sandwich",
                    "Sandwich", "Club sandwich", "Chicken sandwich", "Tuna fish sandwich",
                    "Ham and cheese sandwich", "Breakfast sandwich", "Cheese sandwich"),                                    // estimate

            // ================= MEAT & SIDES =================
            FoodItem.fromPer100("Fried Chicken (Ayam Goreng)", MEAT, G, 260, 150, "1 piece",
                    "Ayam goreng", "Fried chicken", "Crispy fried chicken"),
            FoodItem.fromPer100("Grilled Chicken (Ayam Bakar)", MEAT, G, 180, 150, "1 piece",
                    "Ayam bakar", "Tandoori chicken"),                                                                    // estimate
            FoodItem.fromServing("Chicken Breast (skinless)", MEAT, G, 100, "1 portion", 165),
            FoodItem.fromPer100("Ayam Masak Merah", MEAT, G, 190, 150, "1 portion", "Ayam masak merah"),                 // estimate
            FoodItem.fromPer100("Chicken Curry (Kari Ayam)", MEAT, G, 150, 200, "1 portion",
                    "Chicken curry", "Butter chicken", "Red curry", "Green curry", "Japanese curry", "Mutton curry"),       // estimate
            FoodItem.fromServing("Beef Rendang", MEAT, G, 200, "1 portion", 470, "Rendang"),
            FoodItem.fromPer100("Satay", MEAT, G, 180, 20, "1 stick", "Satay"),                                          // estimate
            FoodItem.fromPer100("Fried Fish (Ikan Goreng)", MEAT, G, 200, 150, "1 piece", "Ikan goreng", "Fried fish"),   // estimate
            FoodItem.fromPer100("Asam Pedas Fish", MEAT, G, 100, 250, "1 bowl", "Asam pedas"),                           // estimate
            FoodItem.fromPer100("Fish Head Curry", MEAT, G, 120, 300, "1 bowl", "Fish head curry"),                      // estimate
            FoodItem.fromPer100("Fried Tofu (Tahu Goreng)", MEAT, G, 271, 100, "1 portion", "Tahu goreng", "Tahu sumedang"),
            FoodItem.fromPer100("Yong Tau Foo (soup)", MEAT, G, 90, 400, "1 bowl", "Yong tau foo", "Fish ball"),         // estimate
            FoodItem.fromPer100("Bak Kut Teh", MEAT, G, 110, 400, "1 bowl", "Bak kut teh"),                              // estimate
            FoodItem.fromPer100("Dumpling / Siu Mai", MEAT, G, 210, 30, "1 piece", "Dumpling", "Shumai", "Taro dumpling"),// estimate
            FoodItem.fromPer100("Pau (Steamed Bun)", MEAT, G, 250, 80, "1 bun", "Baozi"),                                // estimate
            FoodItem.fromPer100("Gado-Gado", MEAT, G, 130, 250, "1 plate", "Gado-gado"),                                 // estimate

            // ================= WESTERN & FAST FOOD =================
            FoodItem.fromServing("Burger", WESTERN, G, 215, "1 burger", 491,
                    "Hamburger", "Cheeseburger", "Chili burger", "Buffalo burger", "Veggie burger"),
            FoodItem.fromPer100("French Fries", WESTERN, G, 312, 110, "1 medium", "Cheese fries", "Home fries", "Potato wedges"),
            FoodItem.fromPer100("Pizza", WESTERN, G, 266, 107, "1 slice",
                    "New York-style pizza", "Chicago-style pizza", "Neapolitan pizza", "California-style pizza",
                    "Detroit-style pizza", "St. Louis-style pizza", "New Haven-style pizza"),
            FoodItem.fromPer100("Chicken Nuggets", WESTERN, G, 296, 100, "6 pieces", "Chicken nugget", "Chicken fingers"),
            FoodItem.fromPer100("Sushi", WESTERN, G, 150, 30, "1 piece", "Sushi", "Spam musubi"),                       // estimate
            FoodItem.fromPer100("Doughnut", WESTERN, G, 421, 60, "1 doughnut", "Doughnut", "Boston cream doughnut", "Maple bacon donut"),
            FoodItem.fromPer100("Cake (1 slice)", WESTERN, G, 370, 80, "1 slice",
                    "Cheesecake", "Red velvet cake", "Cupcake", "Pound cake", "Chocolate brownie",
                    "Coconut cake", "German chocolate cake"),                                                              // estimate
            FoodItem.fromPer100("Cookies", WESTERN, G, 488, 30, "3 pieces", "Chocolate chip cookie", "Butter cookie", "Peanut butter cookie"),
            FoodItem.fromPer100("Ice Cream", WESTERN, G, 207, 70, "1 scoop", "Ice cream cake", "Fried ice cream"),

            // ================= SNACKS & DESSERTS =================
            FoodItem.fromServing("Curry Puff (Karipap)", SNACKS, G, 50, "1 piece", 130),
            FoodItem.fromServing("Pisang Goreng", SNACKS, G, 50, "1 piece", 120),
            FoodItem.fromServing("Apam Balik", SNACKS, G, 100, "1 piece", 260),
            FoodItem.fromPer100("Kuih (assorted)", SNACKS, G, 230, 50, "1 piece", "Kuih", "Seri Muka", "Kalu dodol", "Bibingka"), // estimate
            FoodItem.fromPer100("Popiah (fresh)", SNACKS, G, 130, 120, "1 roll", "Popiah"),                              // estimate
            FoodItem.fromServing("Cendol", SNACKS, G, 350, "1 bowl", 300),
            FoodItem.fromServing("ABC (Ais Kacang)", SNACKS, G, 400, "1 bowl", 300, "Ais kacang"),
            FoodItem.fromPer100("Tong Sui (Sweet Soup)", SNACKS, G, 90, 250, "1 bowl", "Tong sui", "Grass jelly"),       // estimate

            // ================= FRUITS (USDA) =================
            FoodItem.fromPer100("Banana", FRUITS, G, 89, 118, "1 medium"),
            FoodItem.fromPer100("Apple", FRUITS, G, 52, 180, "1 medium"),
            FoodItem.fromPer100("Orange", FRUITS, G, 47, 130, "1 medium"),
            FoodItem.fromPer100("Watermelon", FRUITS, G, 30, 280, "1 slice"),
            FoodItem.fromPer100("Papaya", FRUITS, G, 43, 150, "1 cup"),
            FoodItem.fromPer100("Mango", FRUITS, G, 60, 165, "1 cup"),
            FoodItem.fromPer100("Pineapple", FRUITS, G, 50, 165, "1 cup"),
            FoodItem.fromPer100("Guava", FRUITS, G, 68, 100, "1 small"),
            FoodItem.fromPer100("Grapes", FRUITS, G, 69, 150, "1 cup"),
            FoodItem.fromPer100("Durian", FRUITS, G, 147, 100, "3 seeds"),
            FoodItem.fromPer100("Dates (Kurma)", FRUITS, G, 282, 7, "1 date"),

            // ================= DRINKS (per 100 ml) =================
            FoodItem.fromServing("Teh Tarik", DRINKS, ML, 250, "1 glass", 170),
            FoodItem.fromServing("Teh O Kosong", DRINKS, ML, 250, "1 glass", 15),
            FoodItem.fromServing("Kopi O (with sugar)", DRINKS, ML, 250, "1 glass", 40),
            FoodItem.fromPer100("Kopi (with condensed milk)", DRINKS, ML, 60, 250, "1 glass"),                          // estimate
            FoodItem.fromServing("Milo Ais", DRINKS, ML, 300, "1 glass", 250),
            FoodItem.fromServing("Air Bandung", DRINKS, ML, 300, "1 glass", 200),
            FoodItem.fromServing("Coconut Water", DRINKS, ML, 250, "1 glass", 45),
            FoodItem.fromServing("Sugarcane Juice", DRINKS, ML, 250, "1 glass", 180),
            FoodItem.fromPer100("Orange Juice", DRINKS, ML, 45, 250, "1 glass"),
            FoodItem.fromPer100("Full Cream Milk", DRINKS, ML, 61, 250, "1 glass"),
            FoodItem.fromPer100("Soft Drink (Cola)", DRINKS, ML, 42, 325, "1 can"),
            FoodItem.fromPer100("Bubble Milk Tea", DRINKS, ML, 70, 500, "1 cup")                                         // estimate
    );

    public static List<FoodItem> getAll() {
        return FOODS;
    }

    /** Foods whose name contains the search text and that belong to the chosen category. */
    public static List<FoodItem> search(String query, String category) {
        String q = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        List<FoodItem> results = new ArrayList<>();
        for (FoodItem food : FOODS) {
            boolean categoryOk = ALL.equals(category) || food.category.equals(category);
            boolean textOk = q.isEmpty() || food.name.toLowerCase(Locale.ROOT).contains(q);
            if (categoryOk && textOk) {
                results.add(food);
            }
        }
        return results;
    }

    /** Foods in our list that match a label predicted by the AI model. */
    public static List<FoodItem> findByModelLabel(String label) {
        List<FoodItem> results = new ArrayList<>();
        for (FoodItem food : FOODS) {
            if (food.matchesModelLabel(label)) {
                results.add(food);
            }
        }
        return results;
    }
}
