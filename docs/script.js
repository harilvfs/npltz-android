(function () {
  var root = document.documentElement;

  /* ------------------------------------------------ language */

  var NE = {
    skip: "सामग्रीमा जानुहोस्",
    nav_features: "विशेषताहरू",
    nav_screenshots: "स्क्रिनसट",
    nav_themes: "थिम",
    nav_changelog: "परिवर्तन",

    hero_eyebrow: "एन्ड्रोइडका लागि विक्रम संवत् पात्रो",
    hero_h1: "नेपाली पात्रो, अनावश्यक कुरा बिना।",
    hero_lede:
      "Npltz ले आजको वि.सं. मिति, त्यससँग मिल्ने ई.सं. मिति, कार्यक्रम र सम्झनाहरू सानो, नेटिभ एन्ड्रोइड एपमा देखाउँछ। कुनै साइन इन छैन, कुनै लोडिङ छैन, कुनै ट्र्याकिङ छैन।",
    cta_download: "APK डाउनलोड",
    cta_source: "स्रोत हेर्नुहोस्",
    fineprint:
      "एन्ड्रोइड 8.0 वा माथि। निःशुल्क र खुला स्रोत, GPL-3.0। Material 3 मा बनेको।",

    stat_apk_n: "1.8 MB",
    stat_apk_l: "हस्ताक्षरित APK",
    stat_off_n: "100% अफलाइन",
    stat_off_l: "इन्टरनेट अनुमति छैन",
    stat_years_n: "1975 देखि 2090",
    stat_years_l: "वि.सं. वर्षहरू भित्रै",
    stat_themes_n: "9 थिम",
    stat_themes_l: "Material You रंगहरू",

    feat_eyebrow: "विशेषताहरू",
    feat_h2: "पात्रो मात्र, अरु केही होइन।",
    feat_lede:
      "विक्रम संवत् मितिहरू ट्र्याक गर्न आवश्यक सबै कुरा, र अनावश्यक केही पनि होइन।",
    card1_t: "महिना र वर्ष दृश्य",
    card1_d:
      "पूरै वि.सं. महिना वा वर्ष हेर्नुहोस्। हरेक दिनमुनि त्यससँग मिल्ने ई.सं. मिति पनि हुन्छ।",
    card2_t: "दिनको विवरण",
    card2_d:
      "कुनै पनि मिति खोलेर हप्ता नम्बर, बाँकी दिन र वर्षको प्रगति एकै नजरमा हेर्नुहोस्।",
    card3_t: "कार्यक्रम र टिप्पणी",
    card3_d: "मितिमा कार्यक्रम र टिप्पणी जोड्नुहोस्, चाहेमा सम्झना समय पनि।",
    card4_t: "सूचनाहरू",
    card4_d:
      "दैनिक मिति सूचना, र चाहेमा आजको मिति सधैं सूचनामा राख्ने स्थायी सूचना।",
    card5_t: "होम स्क्रिन विजेट",
    card5_d:
      "मिति, महिना र अभिवादन विजेट, सबै तपाईंको हालको थिममा।",
    card6_t: "थिम र अक्षर",
    card6_d:
      "नौ रंग थिम, तीन फन्ट, चार अक्षर आकार, अंग्रेजी र नेपाली लिपि।",

    privacy_t: "डिजाइनदेखि नै अफलाइन",
    privacy_p:
      "वि.सं. पात्रोको डाटा एपभित्रै समावेश गरिएको छ, जसले 1975 देखि 2090 वि.सं. समेट्छ। कुनै API छैन, खाता छैन, विज्ञापन छैन, एनालिटिक्स छैन, र म्यानिफेस्टमा इन्टरनेट अनुमति पनि छैन। तपाईंको फोनबाट केही पनि बाहिर जाँदैन।",
    chip_ads: "विज्ञापन छैन",
    chip_trackers: "ट्र्याकर छैन",
    chip_signin: "साइन इन छैन",

    shots_eyebrow: "स्क्रिनसट",
    shots_h2: "हरेक स्क्रिन, तपाईंकै थिममा।",
    shots_lede:
      "तलको थिम फेर्दा पूरै एपको रंग फेरिन्छ, महिना ग्रिडदेखि सेटिङ्स पृष्ठसम्म।",
    cap_month: "महिना दृश्य",
    cap_year: "वर्ष दृश्य",
    cap_day: "दिनको विवरण",
    cap_events: "कार्यक्रम र टिप्पणी",
    cap_widgets: "विजेट",
    cap_notifications: "सूचना",
    rail_note: "स्क्रिनसटहरू पूर्वनिर्धारित Catppuccin Mocha थिममा छन्।",

    themes_eyebrow: "थिम",
    themes_h2: "नौ थिम। एउटा छान्नुहोस्।",
    themes_lede:
      "यी एपसँगै आउने रंगहरू हुन्। कुनै रंगमा क्लिक गर्नुहोस्, यो पृष्ठ पनि त्यही रंगमा फेरिन्छ। तपाईंको छनोट यो ब्राउजरमा सम्झिन्छ।",

    dl_h2: "Npltz लिनुहोस्",
    dl_p:
      "एन्ड्रोइड 8.0 वा माथिका लागि 1.8 MB को हस्ताक्षरित APK। F-Droid मा पेश गरिएको छ र समीक्षामा छ। GitHub Releases अहिले उपलब्ध छ, SHA-256 चेकसमसहित।",
    cta_releases: "सबै रिलिज",

    foot_brand1: "नेपाली मितिहरू, शान्त रूपमा।",
    foot_brand2: "नेपालमा बनेको।",
    foot_download: "डाउनलोड",
    foot_latest: "पछिल्लो रिलिज",
    foot_apk: "सिधा APK",
    foot_sum: "चेकसम",
    foot_soon: "F-Droid",
    foot_in_review: "समीक्षामा",
    foot_learn: "थप जान्न",
    foot_calendar: "पात्रो डाटा",
    foot_project: "परियोजना",
    foot_source: "स्रोत कोड",
    foot_changelog: "परिवर्तनहरू",
    foot_license: "इजाजतपत्र",
    foot_bugs: "बग रिपोर्ट",
    foot_author: "लेखक",
    foot_email: "इमेल",
    foot_terminal: "टर्मिनलका लागि npltz",
    foot_copy: "हरि चालिसे। GPL-3.0।",

    meta_title: "Npltz | एन्ड्रोइडका लागि नेपाली (विक्रम संवत्) पात्रो",
    meta_desc:
      "Npltz एन्ड्रोइडका लागि सफा, अफलाइन नेपाली (विक्रम संवत्) पात्रो हो, विजेट, कार्यक्रम र नौ थिमसहित। 1.8 MB, विज्ञापन छैन, ट्र्याकिङ छैन, इन्टरनेट अनुमति छैन।",

    aria_nav: "मुख्य",
    aria_lang: "भाषा",
    aria_themes: "रंग थिम",
    aria_github: "Npltz GitHub मा",
    aria_back_top: "माथि जानुहोस्"
  };

  var i18nNodes = Array.prototype.slice.call(document.querySelectorAll("[data-i18n]"));
  var i18nAria = Array.prototype.slice.call(document.querySelectorAll("[data-i18n-aria]"));
  var baseText = new Map();
  var baseAria = new Map();

  i18nNodes.forEach(function (el) {
    baseText.set(el, el.textContent);
  });
  i18nAria.forEach(function (el) {
    baseAria.set(el, el.getAttribute("aria-label") || "");
  });

  var baseTitle = document.title;
  var metaDesc = document.querySelector('meta[name="description"]');
  var baseDesc = metaDesc ? metaDesc.getAttribute("content") : "";
  var langButtons = document.querySelectorAll("[data-lang]");

  function applyLang(lang, remember) {
    var ne = lang === "ne";
    root.lang = ne ? "ne" : "en";

    i18nNodes.forEach(function (el) {
      var key = el.getAttribute("data-i18n");
      el.textContent = ne && NE[key] ? NE[key] : baseText.get(el);
    });

    i18nAria.forEach(function (el) {
      var key = el.getAttribute("data-i18n-aria");
      el.setAttribute("aria-label", ne && NE[key] ? NE[key] : baseAria.get(el));
    });

    langButtons.forEach(function (button) {
      button.setAttribute("aria-pressed", button.dataset.lang === lang ? "true" : "false");
    });

    document.title = ne ? NE.meta_title : baseTitle;
    if (metaDesc) metaDesc.setAttribute("content", ne ? NE.meta_desc : baseDesc);

    var toTop = document.getElementById("to-top");
    if (toTop) toTop.title = ne ? NE.aria_back_top : "Back to top";

    if (remember) {
      try {
        localStorage.setItem("npltz-lang", lang);
      } catch (e) {}
    }
  }

  langButtons.forEach(function (button) {
    button.addEventListener("click", function () {
      applyLang(button.dataset.lang, true);
    });
  });

  var savedLang = "en";
  try {
    savedLang = localStorage.getItem("npltz-lang") === "ne" ? "ne" : "en";
  } catch (e) {}
  applyLang(savedLang, false);

  /* ------------------------------------------------ theme */

  var themeColor = document.querySelector('meta[name="theme-color"]');
  var buttons = document.querySelectorAll("[data-set-theme]");

  function apply(theme, remember) {
    root.dataset.theme = theme;
    buttons.forEach(function (button) {
      button.setAttribute(
        "aria-pressed",
        button.dataset.setTheme === theme ? "true" : "false"
      );
    });
    if (themeColor) {
      var surface = getComputedStyle(root).getPropertyValue("--surface").trim();
      if (surface) themeColor.setAttribute("content", surface);
    }
    if (remember) {
      try {
        localStorage.setItem("npltz-theme", theme);
      } catch (e) {}
    }
  }

  buttons.forEach(function (button) {
    button.addEventListener("click", function () {
      apply(button.dataset.setTheme, true);
    });
  });

  var current = root.dataset.theme;
  var known = false;
  buttons.forEach(function (button) {
    if (button.dataset.setTheme === current) known = true;
  });
  apply(known ? current : "catppuccin-mocha", false);

  var year = document.getElementById("year");
  if (year) year.textContent = String(new Date().getFullYear());

  var header = document.querySelector(".site-header");
  var toTop = document.getElementById("to-top");
  var doc = document.documentElement;
  var pending = false;
  var maxScroll = 0;
  var nativeProgress =
    !!window.CSS &&
    !!CSS.supports &&
    CSS.supports("animation-timeline: scroll(root block)");

  function measure() {
    maxScroll = doc.scrollHeight - window.innerHeight;
  }

  function onScroll() {
    var top = window.scrollY || doc.scrollTop || 0;
    if (header && !nativeProgress) {
      var progress = maxScroll > 0 ? Math.min(1, top / maxScroll) : 0;
      header.style.setProperty("--scroll-progress", progress.toFixed(4));
    }
    if (toTop) toTop.classList.toggle("is-visible", top > 560);
    pending = false;
  }

  window.addEventListener(
    "scroll",
    function () {
      if (!pending) {
        pending = true;
        window.requestAnimationFrame(onScroll);
      }
    },
    { passive: true }
  );
  window.addEventListener(
    "resize",
    function () {
      measure();
      onScroll();
    },
    { passive: true }
  );
  window.addEventListener("load", function () {
    measure();
    onScroll();
  });
  if ("ResizeObserver" in window) {
    new ResizeObserver(measure).observe(document.body);
  }
  measure();
  onScroll();

  if (toTop) {
    toTop.addEventListener("click", function () {
      var reduce = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
      window.scrollTo({ top: 0, behavior: reduce ? "auto" : "smooth" });
    });
  }

  var starBox = document.querySelector("[data-stars]");
  var starValue = document.querySelector("[data-stars-value]");
  if (starBox && starValue && window.fetch) {
    fetch("https://api.github.com/repos/harilvfs/npltz-android", {
      headers: { Accept: "application/vnd.github+json" },
      cache: "no-store",
    })
      .then(function (res) {
        return res.ok ? res.json() : null;
      })
      .then(function (data) {
        var count = data && data.stargazers_count;
        if (!count) return;
        starValue.textContent =
          count >= 1000 ? (count / 1000).toFixed(1).replace(".0", "") + "k" : String(count);
        starBox.hidden = false;
      })
      .catch(function () {});
  }

  var rail = document.querySelector(".rail");
  if (rail) {
    var drag = null;

    rail.addEventListener("pointerdown", function (e) {
      if (e.pointerType !== "mouse" || e.button !== 0) return;
      drag = { x: e.clientX, left: rail.scrollLeft };
      rail.classList.add("is-dragging");
      e.preventDefault();
    });

    window.addEventListener("pointermove", function (e) {
      if (!drag) return;
      rail.scrollLeft = drag.left - (e.clientX - drag.x);
    });

    window.addEventListener("pointerup", function () {
      if (!drag) return;
      drag = null;
      rail.classList.remove("is-dragging");
    });
  }
})();
