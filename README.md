# ساده‌سازی توابع منطقی با الگوریتم کواین-مک‌کلاسکی در جاوا

این پروژه یک پیاده‌سازی از روش **کواین-مک‌کلاسکی (Quine-McCluskey)** به زبان جاوا است که برای ساده‌سازی توابع منطقی بولی به کار می‌رود. این الگوریتم یک روش جدولی است که تضمین می‌کند بهینه‌ترین حالت جمع حاصل‌ضرب‌ها (Sum of Products) برای یک تابع بولی را پیدا کند.

برنامه ابتدا تعداد متغیرها و سپس مینترم‌های تابع را از ورودی دریافت کرده و در خروجی، عبارت بولی ساده‌شده را نمایش می‌دهد.

**مشارکت‌کنندگان:**
* کسری تات - ۴۰۳۱۴۱۹۳
* سجاد باقری - ۴۰۳۱۲۷۸۳
* عرفان رنجبر - ۴۰۳۱۸۸۸۳

### مثال نحوه استفاده:
'''text
Do you want to enter the number of variables? (yes/no)
yes
Number of variables (max 6): 3
Enter minterms : 
1 3 5 7

Simplified Function:
C
'''
---

## الگوریتم‌های اصلی پیاده‌سازی شده

### ۱. الگوریتم Quine-McCluskey
این الگوریتم هسته اصلی برنامه است. هدف آن یافتن تمام **ایمپلیکنت‌های اول (Prime Implicants)** یک تابع منطقی است. ایمپلیکنت اول، یک عبارت حاصل‌ضرب است که دیگر نمی‌توان آن را با ترکیب با عبارات دیگر ساده‌تر کرد.

**خلاصه عملکرد:**
* **گروه‌بندی:** ابتدا تمام مینترم‌ها بر اساس تعداد بیت‌های `1` در نمایش باینری آن‌ها گروه‌بندی می‌شوند.
* **ترکیب:** عبارات موجود در گروه‌های مجاور (که تعداد بیت `1` آن‌ها یک واحد اختلاف دارد) با یکدیگر مقایسه می‌شوند. اگر دو عبارت فقط در یک بیت تفاوت داشته باشند، با هم ترکیب شده و یک عبارت جدید با علامت `'-'` (Don't Care) در محل بیت متفاوت تولید می‌شود.
* **تکرار:** این فرآیند ترکیب به‌صورت تکراری برای گروه‌های جدید ادامه می‌یابد تا زمانی که هیچ ترکیب جدیدی ممکن نباشد.
* **نتیجه:** عباراتی که در هیچ مرحله‌ای ترکیب نشده‌اند، به عنوان ایمپلیکنت‌های اول شناخته می‌شوند.



'''java
// مرحله اصلی ترکیب عبارات در یک حلقه تکرار می‌شود

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

                    if (!newGroups.get(ones).contains(merged))
                        newGroups.get(ones).add(merged);

                    marked.add(a);
                    marked.add(b);
                    combined = true;
                }
            }
        }
    }

    // افزودن عبارات ترکیب‌نشده به لیست ایمپلیکنت‌های اول
    for (List<String> group : groups.values()) {
        for (String term : group) {
            if (!marked.contains(term)) {
                primeImplicants.add(term);
            }
        }
    }

    groups = newGroups;  // به‌روزرسانی گروه‌ها برای مرحله بعد

    } while (combined); // تا زمانی که ترکیبی انجام شود ادامه بده
'''


### ۲. انتخاب ایمپلیکنت‌های اول ضروری (Essential Prime Implicants)
پس از یافتن تمام ایمپلیکنت‌های اول، باید زیرمجموعه‌ای از آن‌ها را انتخاب کنیم که تمام مینترم‌های اصلی را پوشش دهد. اولین و مهم‌ترین قدم، یافتن **ایمپلیکنت‌های اول ضروری (EPI)** است. یک EPI، ایمپلیکنتی است که حداقل یک مینترم را پوشش می‌دهد که توسط هیچ ایمپلیکنت دیگری پوشش داده نمی‌شود.

**خلاصه عملکرد:**
* **ساخت جدول پوشش:** یک جدول مجازی ساخته می‌شود که در آن برای هر مینترم مشخص می‌شود که توسط کدام ایمپلیکنت‌های اول پوشش داده می‌شود.
* **بررسی:** جدول بررسی می‌شود تا مینترم‌هایی که تنها توسط یک ایمپلیکنت اول پوشش داده شده‌اند، پیدا شوند.
* **انتخاب:** آن ایمپلیکنت اولی که به تنهایی یک مینترم را پوشش می‌دهد، به عنوان "ضروری" علامت‌گذاری شده و به راه‌حل نهایی اضافه می‌شود.



'''java
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
            covered.addAll(implicantMap.get(only));
        }
    }
}
'''

---
## توابع کلیدی به کار رفته در کد

1.  **`toBinary`**
    * **کاربرد:** تبدیل یک عدد صحیح دهدهی به یک رشته باینری با تعداد بیت مشخص. در صورت نیاز، صفرهای پیشرو به آن اضافه می‌کند.
    '''java
    static String toBinary(int num, int bits) {
        String bin = Integer.toBinaryString(num);
        while (bin.length() < bits) bin = "0" + bin;
        return bin;
    }
    '''

2.  **`countOnes`**
    * **کاربرد:** شمارش تعداد بیت‌های `1` در یک رشته باینری ورودی.
    '''java
    static int countOnes(String bin) {
        int count = 0;
        for (char c : bin.toCharArray()) if (c == '1') count++;
        return count;
    }
    '''

3.  **`diffByOne`**
    * **کاربرد:** بررسی می‌کند که آیا دو رشته باینری دقیقاً در یک بیت با هم تفاوت دارند یا خیر.
    '''java
    static int diffByOne(String a, String b) {
        int diff = 0;
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) != b.charAt(i)) diff++;
        }
        return diff;
    }
    '''

4.  **`merge`**
    * **کاربرد:** دو رشته باینری را که فقط در یک بیت تفاوت دارند، با هم ترکیب کرده و در محل بیت متفاوت، کاراکتر `'-'` قرار می‌دهد.
    '''java
    static String merge(String a, String b) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < a.length(); i++) {
            if (a.charAt(i) == b.charAt(i)) sb.append(a.charAt(i));
            else sb.append('-');
        }
        return sb.toString();
    }
    '''

5.  **`toExpression`**
    * **کاربرد:** یک عبارت باینری (شامل `-`) را به یک عبارت جبری خوانا (مانند `A'BC`) تبدیل می‌کند.
    '''java
    static String toExpression(String term) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < term.length(); i++) {
            if (term.charAt(i) == '-') continue;
            char var = (char) ('A' + i);
            sb.append(var);
            if (term.charAt(i) == '0') sb.append("'");
        }
        return sb.toString();
    }
    '''

---
## کلاس‌ها و کتابخانه‌های اصلی جاوا

1.  **`java.util.Scanner`**
    * یک کلاس ساده برای خواندن ورودی از جریآن‌های مختلف، از جمله ورودی استاندارد (کنسول). در این پروژه برای دریافت تعداد متغیرها و لیست مینترم‌ها از کاربر استفاده شده است.

2.  **`java.util.List` و `java.util.ArrayList`**
    * `List` یک رابط برای مجموعه‌های مرتب است. `ArrayList` پیاده‌سازی متداولی از این رابط است که از یک آرایه پویا استفاده می‌کند. در این کد برای ذخیره مینترم‌ها و گروه‌های مختلف عبارات باینری به کار رفته است.

3.  **`java.util.Map` و `java.util.HashMap` / `TreeMap`**
    * `Map` یک ساختار داده برای ذخیره زوج‌های کلید-مقدار است.
    * `TreeMap` کلیدها را مرتب نگه می‌دارد و برای گروه‌بندی اولیه مینترم‌ها بر اساس تعداد `1`ها مناسب است.
    * `HashMap` ترتیب خاصی را تضمین نمی‌کند اما عملکرد سریعی دارد و برای ساخت جدول پوشش (نگاشت مینترم به ایمپلیکنت‌ها) استفاده شده است.

4.  **`java.util.Set` و `java.util.HashSet`**
    * `Set` یک مجموعه از داده‌ها است که هیچ عنصر تکراری را نمی‌پذیرد. `HashSet` پیاده‌سازی رایج آن است. این ساختار داده برای ذخیره ایمپلیکنت‌های اول و ایمپلیکنت‌های ضروری بسیار مفید است، زیرا به طور خودکار از افزوده شدن موارد تکراری جلوگیری می‌کند.
