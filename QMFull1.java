import java.util.*;

public class QMFull1 {

    // تبدیل عدد صحیح به رشته باینری با طول مشخص (با اضافه کردن صفرهای پیش‌رو)
    static String toBinary(int num, int bits) {
        String bin = Integer.toBinaryString(num);
        // اضافه کردن صفر به ابتدا تا رسیدن به طول مورد نظر
        while (bin.length() < bits) bin = "0" + bin;
        return bin;
    }

    // شمارش تعداد بیت‌های 1 در رشته باینری
    static int countOnes(String bin) {
        int count = 0;
        for (char c : bin.toCharArray()) if (c == '1') count++;
        return count;
    }

    // محاسبه تعداد تفاوت‌های بیتی بین دو رشته باینری
    static int diffByOne(String a, String b) {
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) diff++;
        }
        return diff;
    }

    // ترکیب دو رشته باینری با جایگزینی بیت‌های متفاوت با '-' (شرط ترکیب پذیری)
    static String merge(String a, String b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) == b.charAt(i)) sb.append(a.charAt(i));
            else sb.append('-');  // جایگزینی تفاوت با don't care
        }
        return sb.toString();
    }

    // تبدیل عبارت باینری به فرمول جبری (مثال: "01-" → "A'B")
    static String toExpression(String term) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < term.length(); i++) {
            if (term.charAt(i) == '-') continue;  // نادیده گرفتن don't care

            char var = (char) ('A' + i);  // نام متغیر: A, B, C, ...
            sb.append(var);
            // اگر بیت 0 باشد ' اضافه می‌کند (مکمل)
            if (term.charAt(i) == '0') sb.append("'");
        }
        return sb.toString();
    }

    // محاسبه مجموعه مینترم‌های پوشش داده شده توسط یک عبارت
    static Set<Integer> getMinterms(String term) {
        Set<String> results = new HashSet<>();
        // تولید تمام ترکیب‌های ممکن برای بیت‌های don't care
        generateTerms(term, "", results);

        Set<Integer> nums = new HashSet<>();
        // تبدیل رشته باینری به عدد صحیح
        for (String bin : results) {
            nums.add(Integer.parseInt(bin, 2));
        }
        return nums;
    }

    // تابع بازگشتی برای تولید ترکیب‌های باینری از الگوی ورودی
    static void generateTerms(String pattern, String current, Set<String> results) {
        // شرط توقف: رسیدن به طول الگو
        if (current.length() == pattern.length()) {
            results.add(current);
            return;
        }

        char ch = pattern.charAt(current.length());
        if (ch == '-') {
            // ایجاد دو شاخه برای جایگزینی 0 و 1
            generateTerms(pattern, current + "0", results);
            generateTerms(pattern, current + "1", results);
        } else {
            // استفاده از بیت ثابت الگو
            generateTerms(pattern, current + ch, results);
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // دریافت تعداد متغیرها از کاربر
        System.out.println("Do you want to enter the number of variables? (yes/no)");
        String choice = scanner.nextLine().trim().toLowerCase();
        int vars;

        if (choice.equals("yes") || choice.equals("y")) {
            System.out.print("Number of variables (max 6): ");
            vars = scanner.nextInt();
            scanner.nextLine(); // مصرف کاراکتر جدید خط
        } else {
            vars = -1;  // علامت برای محاسبه خودکار
        }

        // دریافت مینترم‌ها از کاربر
        System.out.println("Enter minterms : ");
        String[] mintermsStr = scanner.nextLine().trim().split("\\s+"); // اطمینان از جدا کردن درست ورودی‌ها
        int maxMinterm = (int) Math.pow(2 , vars) - 1;
        List<Integer> mintermsList = new ArrayList<>();

        for (String s : mintermsStr) {
            try {
                int m = Integer.parseInt(s.trim());
                if (m < 0 || m > maxMinterm) {
                    System.out.println("⚠ Invalid minterm: " + m + " — must be between 0 and " + maxMinterm);
                    return;
                }
                mintermsList.add(m);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input: '" + s + "' is not a number.");
                return;
            }
        }

        int[] minterms = mintermsList.stream().mapToInt(i -> i).toArray();

        // مرحله 1: گروه‌بندی مینترم‌ها بر اساس تعداد 1
        Map<Integer, List<String>> groups = new TreeMap<>();
        for (int m : minterms) {
            String bin = toBinary(m, vars);
            int ones = countOnes(bin);
            groups.putIfAbsent(ones, new ArrayList<>());
            groups.get(ones).add(bin);
        }

        Set<String> primeImplicants = new HashSet<>();
        boolean combined;

        // مرحله اصلی ترکیب عبارات
        do {
            combined = false;
            Map<Integer, List<String>> newGroups = new TreeMap<>();
            Set<String> marked = new HashSet<>();  // عبارات ترکیب شده

            // مقایسه هر گروه با گروه بعدی
            for (int i : groups.keySet()) {
                List<String> group1 = groups.get(i);
                List<String> group2 = groups.get(i + 1);
                if (group2 == null) continue;

                // مقایسه هر عبارت در گروه فعلی با گروه بعدی
                for (String a : group1) {
                    for (String b : group2) {
                        // اگر دقیقاً یک بیت تفاوت داشته باشند
                        if (diffByOne(a, b) == 1) {
                            String merged = merge(a, b);
                            int ones = countOnes(merged.replace("-", ""));
                            newGroups.putIfAbsent(ones, new ArrayList<>());

                            // جلوگیری از اضافه شدن عبارت تکراری
                            if (!newGroups.get(ones).contains(merged))
                                newGroups.get(ones).add(merged);

                            marked.add(a);
                            marked.add(b);
                            combined = true;  // حداقل یک ترکیب انجام شد
                        }
                    }
                }
            }

            // افزودن عبارات ترکیب‌نشدهPl به
            for (List<String> group : groups.values()) {
                for (String term : group) {
                    if (!marked.contains(term)) {
                        primeImplicants.add(term);
                    }
                }
            }

            groups = newGroups;  // به روزرسانی گروه‌ها برای مرحله بعد

        } while (combined);  // تا زمانی که ترکیبی انجام شود ادامه بده

        // ساختPI → مینترم‌های پوشش داده شده
        Map<String, Set<Integer>> implicantMap = new HashMap<>();
        for (String imp : primeImplicants) {
            implicantMap.put(imp, getMinterms(imp));
        }

        // ساخت جدول پوشش معکوس (مینترم → PIهای پوشش دهنده)
        Map<Integer, Set<String>> coverageMap = new HashMap<>();
        for (int m : minterms) {
            coverageMap.put(m, new HashSet<>());
            for (String imp : implicantMap.keySet()) {
                if (implicantMap.get(imp).contains(m)) {
                    coverageMap.get(m).add(imp);
                }
            }
        }

        // یافتن EPI
        Set<String> essential = new HashSet<>();
        Set<Integer> covered = new HashSet<>();  // مینترم‌های پوشش داده شده

        for (int m : coverageMap.keySet()) {
            Set<String> covers = coverageMap.get(m);
            // اگر فقط یک PI این مینترم را بپوشاند
            if (covers.size() == 1) {
                String only = covers.iterator().next();
                if (!essential.contains(only)) {
                    essential.add(only);
                    covered.addAll(implicantMap.get(only));  // افزودن مینترم‌های پوشش یافته
                }
            }
        }

        // نمایش نتیجه نهایی (فقط Essentialها)
        System.out.println("\nSimplified Function:");
        List<String> result = new ArrayList<>();
        for (String term : essential) {
            result.add(toExpression(term));  // تبدیل به فرمول خوانا
        }
        System.out.println(String.join(" + ", result));  // چاپ با فرمت SOP
    }
}

//-----------------------------------
// 403 14 193       کسری تات
// 403 12 783       سجاد باقری
//403 18 883       عرفان رنجبر
//-----------------------------------
