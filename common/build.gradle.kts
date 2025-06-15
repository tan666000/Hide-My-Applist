import com.android.build.api.dsl.Packaging
import java.util.*
import java.io.File

// officialBuild 的定義應自 rootProject.extra 或者通過 local.properties 注入
val officialBuild: Boolean by rootProject.extra

plugins {
    alias(libs.plugins.agp.app)
    alias(libs.plugins.autoresconfig)
    alias(libs.plugins.materialthemebuilder)
    alias(libs.plugins.refine)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.nav.safeargs.kotlin)
    // 移除了 alias(libs.plugins.gms.get().pluginId)
}

// 移除了整個 if (officialBuild) 塊，因為它應用了 gms 插件
// if (officialBuild) {
//     plugins.apply(libs.plugins.gms.get().pluginId)
// }

android {
    namespace = "com.tsng.hidemyapplist" // 請確保此處包名與你的專案實際使用的包名一致

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    packaging {
        dex.useLegacyPackaging = true
        resources {
            excludes += arrayOf(
                "/META-INF/*",
                "/META-INF/androidx/**",
                "/kotlin/**",
                "/okhttp3/**",
            )
        }
    }

    signingConfigs {
        create("release") {
            val properties = Properties()
            val localPropertiesFile = project.rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localPropertiesFile.inputStream().use { properties.load(it) }
            }

            val keystorePath = properties.getProperty("fileDir") ?: ""
            if (keystorePath.isEmpty()) {
                println("WARNING: keystorePath (fileDir) not found in local.properties. Attempting to use default local path 'my-release-key.jks'.")
                storeFile = file("my-release-key.jks")
            } else {
                storeFile = File(keystorePath)
            }

            storePassword = properties.getProperty("storePassword") ?: System.getenv("KEY_STORE_PASSWORD")
            keyAlias = properties.getProperty("keyAlias") ?: System.getenv("ALIAS")
            keyPassword = properties.getProperty("keyPassword") ?: System.getenv("ALIAS_KEY_PASSWORD")

            if (storeFile == null || storePassword == null || keyAlias == null || keyPassword == null) {
                println("ERROR: Missing signing configuration details for 'release' build type.")
            }
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            // ... 保持原樣或根據需要添加其他配置
        }
    }
}

kotlin {
    jvmToolchain(21)
}

autoResConfig {
    generateClass.set(true)
    generateRes.set(false)
    generatedClassFullName.set("icu.nullptr.hidemyapplist.util.LangList")
    generatedArrayFirstItem.set("SYSTEM")
}

materialThemeBuilder {
    themes {
        for ((name, color) in listOf(
            "Red" to "F44336",
            "Pink" to "E91E63",
            "Purple" to "9C27B0",
            "DeepPurple" to "673AB7",
            "Indigo" to "3F51B5",
            "Blue" to "2196F3",
            "LightBlue" to "03A9F4",
            "Cyan" to "00BCD4",
            "Teal" to "009688",
            "Green" to "4FAF50",
            "LightGreen" to "8BC3A4",
            "Lime" to "CDDC39",
            "Yellow" to "FFEB3B",
            "Amber" to "FFC107",
            "Orange" to "FF9800",
            "DeepOrange" to "FF5722",
            "Brown" to "795548",
            "BlueGrey" to "607D8F",
            "Sakura" to "FF9CA8"
        )) {
            create("Material$name") {
                lightThemeFormat = "ThemeOverlay.Light.%s"
                darkThemeFormat = "ThemeOverlay.Dark.%s"
                primaryColor = "#$color"
            }
        }
    }
    generatePalette = true
}

// 已移除 fun afterEval() 函數和 afterEvaluate 塊，以解決循環依賴問題。

dependencies {
    implementation(projects.common)
    runtimeOnly(projects.xposed)
    implementation(libs.com.google.android.gms.play.services.ads)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.com.drakeet.about)
    implementation(libs.com.drakeet.multitype)
    implementation(libs.com.github.kirich1409.viewbindingpropertydelegate)
    implementation(libs.com.github.liujingxing.rxhttp)
    implementation(libs.com.github.liujingxing.rxhttp.converter.serialization)
    implementation(libs.com.github.topjohnwu.libsu.core)
    implementation(libs.com.google.android.material)
    implementation(libs.com.squareup.okhttp3)
    implementation(libs.dev.rikka.hidden.compat)
    implementation(libs.dev.rikka.rikkax.material)
    implementation(libs.dev.rikka.rikkax.material.preference)
    implementation(libs.me.zhanghai.android.appiconloader)
    compileOnly(libs.dev.rikka.hidden.stub)
    ksp(libs.com.github.liujingxing.rxhttp.compiler)
}

configurations.all {
    exclude("androidx.appcompat", "appcompat")
}
