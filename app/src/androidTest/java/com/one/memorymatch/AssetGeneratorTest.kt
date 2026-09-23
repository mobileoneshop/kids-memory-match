package com.one.memorymatch

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.one.memorymatch.data.repository.PackRepository
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class AssetGeneratorTest {

    @Test
    fun generateAppIcons() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val inputFile = File(context.getExternalFilesDir(null), "app_icon_source.webp")
        val sourceBitmap = android.graphics.BitmapFactory.decodeFile(inputFile.absolutePath)
        requireNotNull(sourceBitmap) { "Failed to decode ${inputFile.absolutePath}" }

        val outputDir = File(context.getExternalFilesDir(null), "icons")
        outputDir.mkdirs()

        val densities = mapOf(
            "mdpi" to 48,
            "hdpi" to 72,
            "xhdpi" to 96,
            "xxhdpi" to 144,
            "xxxhdpi" to 192,
            "store" to 512
        )

        for ((name, size) in densities) {
            val scaled = Bitmap.createScaledBitmap(sourceBitmap, size, size, true)
            val outFile = File(outputDir, "ic_launcher_$name.webp")
            FileOutputStream(outFile).use { out ->
                scaled.compress(Bitmap.CompressFormat.WEBP, 95, out)
            }
            if (name == "store") {
                val storePng = File(outputDir, "icon_512.png")
                FileOutputStream(storePng).use { out ->
                    scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }
            scaled.recycle()
        }
        println("ICON_OUTPUT_DIR: ${outputDir.absolutePath}")
    }

    @Test
    fun generateAllNinetySixCardAssets() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val repository = PackRepository(context)

        // Store to app external files dir so no storage permissions are required
        val outputBase = File(context.getExternalFilesDir(null), "cards")
        outputBase.mkdirs()
        println("ASSET_GEN_OUTPUT_DIR: ${outputBase.absolutePath}")

        val packs = repository.packs
        for (pack in packs) {
            val packDir = File(outputBase, pack.id)
            packDir.mkdirs()

            val items = repository.getItemsForPack(pack.id)
            for (item in items) {
                val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                renderCardArt(canvas, pack.id, item.id, pack.primaryColor, pack.darkColor)

                val file = File(packDir, "${item.id}.webp")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.WEBP, 85, out)
                }
                bitmap.recycle()
            }
        }
    }

    private fun renderCardArt(
        canvas: Canvas,
        packId: String,
        itemId: String,
        primaryColorLong: Long,
        darkColorLong: Long
    ) {
        val size = 512f
        val center = size / 2f

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Draw smooth soft background circle per pack theme
        val baseColor = Color.rgb(
            ((primaryColorLong shr 16) and 0xFF).toInt(),
            ((primaryColorLong shr 8) and 0xFF).toInt(),
            (primaryColorLong and 0xFF).toInt()
        )

        // Soft pastel tint of pack color
        val pastelColor = Color.argb(
            45,
            Color.red(baseColor),
            Color.green(baseColor),
            Color.blue(baseColor)
        )

        // Outer circular background disc
        paint.style = Paint.Style.FILL
        paint.shader = RadialGradient(
            center, center, 220f,
            Color.WHITE, pastelColor,
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(center, center, 220f, paint)
        paint.shader = null

        // Decorative ring
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 6f
        paint.color = Color.argb(80, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
        canvas.drawCircle(center, center, 218f, paint)

        // 2. Render item-specific illustration
        paint.style = Paint.Style.FILL
        when (packId) {
            "zoo" -> drawZooItem(canvas, itemId, center)
            "farm" -> drawFarmItem(canvas, itemId, center)
            "sea" -> drawSeaItem(canvas, itemId, center)
            "birds" -> drawBirdItem(canvas, itemId, center)
            "fruits" -> drawFruitItem(canvas, itemId, center)
            "vegetables" -> drawVegetableItem(canvas, itemId, center)
            "vehicles" -> drawVehicleItem(canvas, itemId, center)
            "shapes_colors" -> drawShapeColorItem(canvas, itemId, center)
            else -> drawGenericItem(canvas, itemId, center, baseColor)
        }
    }

    // --- ZOO ANIMALS ---
    private fun drawZooItem(canvas: Canvas, id: String, c: Float) {
        when (id) {
            "lion" -> drawLion(canvas, c)
            "elephant" -> drawElephant(canvas, c)
            "monkey" -> drawMonkey(canvas, c)
            "zebra" -> drawZebra(canvas, c)
            "giraffe" -> drawGiraffe(canvas, c)
            "panda" -> drawPanda(canvas, c)
            "tiger" -> drawTiger(canvas, c)
            "hippo" -> drawHippo(canvas, c)
            "kangaroo" -> drawKangaroo(canvas, c)
            "bear" -> drawBear(canvas, c)
            "crocodile" -> drawCrocodile(canvas, c)
            "penguin" -> drawPenguin(canvas, c)
            else -> drawCuteCircleAnimal(canvas, c, Color.rgb(255, 167, 38), id)
        }
    }

    private fun drawLion(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Mane
        p.color = Color.rgb(230, 81, 0)
        p.style = Paint.Style.FILL
        for (i in 0 until 16) {
            val angle = i * (Math.PI * 2 / 16)
            val mx = c + (130f * Math.cos(angle)).toFloat()
            val my = c + (130f * Math.sin(angle)).toFloat()
            canvas.drawCircle(mx, my, 45f, p)
        }
        canvas.drawCircle(c, c, 130f, p)

        // Ears
        p.color = Color.rgb(255, 179, 0)
        canvas.drawCircle(c - 85f, c - 85f, 32f, p)
        canvas.drawCircle(c + 85f, c - 85f, 32f, p)
        p.color = Color.rgb(255, 204, 128)
        canvas.drawCircle(c - 85f, c - 85f, 18f, p)
        canvas.drawCircle(c + 85f, c - 85f, 18f, p)

        // Face
        p.color = Color.rgb(255, 193, 7)
        canvas.drawCircle(c, c, 100f, p)

        // Muzzle
        p.color = Color.rgb(255, 248, 225)
        canvas.drawOval(RectF(c - 55f, c + 10f, c + 55f, c + 75f), p)

        // Nose
        p.color = Color.rgb(62, 39, 35)
        val nosePath = Path().apply {
            moveTo(c - 22f, c + 20f)
            lineTo(c + 22f, c + 20f)
            lineTo(c, c + 42f)
            close()
        }
        canvas.drawPath(nosePath, p)

        // Mouth & Whiskers
        p.style = Paint.Style.STROKE
        p.strokeWidth = 5f
        canvas.drawLine(c, c + 40f, c, c + 58f, p)
        canvas.drawArc(RectF(c - 25f, c + 42f, c, c + 62f), 0f, 180f, false, p)
        canvas.drawArc(RectF(c, c + 42f, c + 25f, c + 62f), 0f, 180f, false, p)

        // Cheeks & Eyes
        p.style = Paint.Style.FILL
        drawEyes(canvas, c - 45f, c - 20f, c + 45f, c - 20f)
        drawCheeks(canvas, c - 60f, c + 25f, c + 60f, c + 25f)
    }

    private fun drawElephant(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Big ears
        p.color = Color.rgb(176, 190, 197)
        canvas.drawCircle(c - 110f, c - 10f, 75f, p)
        canvas.drawCircle(c + 110f, c - 10f, 75f, p)
        p.color = Color.rgb(207, 216, 220)
        canvas.drawCircle(c - 110f, c - 10f, 50f, p)
        canvas.drawCircle(c + 110f, c - 10f, 50f, p)

        // Head
        p.color = Color.rgb(144, 164, 174)
        canvas.drawCircle(c, c - 20f, 100f, p)

        // Trunk
        val trunk = Path().apply {
            moveTo(c - 25f, c + 15f)
            quadTo(c - 30f, c + 110f, c + 35f, c + 120f)
            quadTo(c + 55f, c + 105f, c + 35f, c + 95f)
            quadTo(c - 5f, c + 85f, c + 25f, c + 15f)
            close()
        }
        canvas.drawPath(trunk, p)

        // Tusks
        p.color = Color.rgb(255, 255, 255)
        canvas.drawOval(RectF(c - 45f, c + 40f, c - 25f, c + 90f), p)
        canvas.drawOval(RectF(c + 25f, c + 40f, c + 45f, c + 90f), p)

        // Eyes & Cheeks
        drawEyes(canvas, c - 45f, c - 40f, c + 45f, c - 40f)
        drawCheeks(canvas, c - 60f, c - 5f, c + 60f, c - 5f)
    }

    private fun drawMonkey(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Ears
        p.color = Color.rgb(109, 76, 65)
        canvas.drawCircle(c - 100f, c - 10f, 40f, p)
        canvas.drawCircle(c + 100f, c - 10f, 40f, p)
        p.color = Color.rgb(255, 204, 188)
        canvas.drawCircle(c - 100f, c - 10f, 25f, p)
        canvas.drawCircle(c + 100f, c - 10f, 25f, p)

        // Head
        p.color = Color.rgb(93, 64, 55)
        canvas.drawCircle(c, c - 10f, 95f, p)

        // Face mask (peach heart/figure 8)
        p.color = Color.rgb(255, 224, 178)
        canvas.drawCircle(c - 35f, c - 30f, 45f, p)
        canvas.drawCircle(c + 35f, c - 30f, 45f, p)
        canvas.drawOval(RectF(c - 65f, c - 15f, c + 65f, c + 65f), p)

        // Nose dots & smile
        p.color = Color.rgb(62, 39, 35)
        canvas.drawCircle(c - 10f, c + 15f, 5f, p)
        canvas.drawCircle(c + 10f, c + 15f, 5f, p)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 6f
        canvas.drawArc(RectF(c - 35f, c + 10f, c + 35f, c + 45f), 10f, 160f, false, p)

        p.style = Paint.Style.FILL
        drawEyes(canvas, c - 35f, c - 30f, c + 35f, c - 30f)
        drawCheeks(canvas, c - 55f, c + 15f, c + 55f, c + 15f)
    }

    private fun drawZebra(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Mane
        p.color = Color.rgb(33, 33, 33)
        for (i in -4..4) {
            canvas.drawRect(c - 15f + i * 20f, c - 130f, c - 5f + i * 20f, c - 60f, p)
        }
        // Head
        p.color = Color.rgb(245, 245, 245)
        canvas.drawOval(RectF(c - 75f, c - 85f, c + 75f, c + 50f), p)

        // Ears
        canvas.drawOval(RectF(c - 70f, c - 135f, c - 40f, c - 75f), p)
        canvas.drawOval(RectF(c + 40f, c - 135f, c + 70f, c - 75f), p)

        // Black stripes
        p.color = Color.rgb(33, 33, 33)
        canvas.drawOval(RectF(c - 65f, c - 125f, c - 45f, c - 85f), p)
        canvas.drawOval(RectF(c + 45f, c - 125f, c + 65f, c - 85f), p)

        val s1 = Path().apply {
            moveTo(c - 75f, c - 30f); lineTo(c - 20f, c - 40f); lineTo(c - 75f, c - 50f); close()
        }
        val s2 = Path().apply {
            moveTo(c + 75f, c - 30f); lineTo(c + 20f, c - 40f); lineTo(c + 75f, c - 50f); close()
        }
        val s3 = Path().apply {
            moveTo(c - 70f, c + 5f); lineTo(c - 25f, c); lineTo(c - 70f, c - 10f); close()
        }
        val s4 = Path().apply {
            moveTo(c + 70f, c + 5f); lineTo(c + 25f, c); lineTo(c + 70f, c - 10f); close()
        }
        canvas.drawPath(s1, p); canvas.drawPath(s2, p); canvas.drawPath(s3, p); canvas.drawPath(s4, p)

        // Muzzle
        p.color = Color.rgb(66, 66, 66)
        canvas.drawOval(RectF(c - 50f, c + 30f, c + 50f, c + 105f), p)
        p.color = Color.rgb(255, 255, 255)
        canvas.drawCircle(c - 18f, c + 65f, 6f, p)
        canvas.drawCircle(c + 18f, c + 65f, 6f, p)

        drawEyes(canvas, c - 40f, c - 45f, c + 40f, c - 45f)
        drawCheeks(canvas, c - 55f, c - 5f, c + 55f, c - 5f)
    }

    private fun drawGiraffe(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Horns
        p.color = Color.rgb(141, 110, 99)
        canvas.drawRect(c - 35f, c - 135f, c - 20f, c - 85f, p)
        canvas.drawRect(c + 20f, c - 135f, c + 35f, c - 85f, p)
        canvas.drawCircle(c - 27.5f, c - 135f, 15f, p)
        canvas.drawCircle(c + 27.5f, c - 135f, 15f, p)

        // Ears
        p.color = Color.rgb(255, 179, 0)
        canvas.drawOval(RectF(c - 85f, c - 95f, c - 40f, c - 60f), p)
        canvas.drawOval(RectF(c + 40f, c - 95f, c + 85f, c - 60f), p)

        // Head & neck
        canvas.drawOval(RectF(c - 60f, c - 85f, c + 60f, c + 40f), p)
        canvas.drawRect(c - 35f, c + 20f, c + 35f, c + 130f, p)

        // Brown Spots
        p.color = Color.rgb(191, 54, 12)
        canvas.drawCircle(c - 20f, c - 45f, 14f, p)
        canvas.drawCircle(c + 25f, c - 30f, 18f, p)
        canvas.drawCircle(c - 15f, c + 55f, 20f, p)
        canvas.drawCircle(c + 15f, c + 95f, 22f, p)

        // Muzzle
        p.color = Color.rgb(255, 224, 178)
        canvas.drawOval(RectF(c - 50f, c - 5f, c + 50f, c + 65f), p)
        p.color = Color.rgb(62, 39, 35)
        canvas.drawCircle(c - 18f, c + 25f, 6f, p)
        canvas.drawCircle(c + 18f, c + 25f, 6f, p)

        drawEyes(canvas, c - 35f, c - 55f, c + 35f, c - 55f)
        drawCheeks(canvas, c - 50f, c - 20f, c + 50f, c - 20f)
    }

    private fun drawPanda(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Black ears
        p.color = Color.rgb(33, 33, 33)
        canvas.drawCircle(c - 85f, c - 85f, 38f, p)
        canvas.drawCircle(c + 85f, c - 85f, 38f, p)

        // White head
        p.color = Color.WHITE
        canvas.drawCircle(c, c, 105f, p)

        // Black eye patches
        p.color = Color.rgb(33, 33, 33)
        canvas.save()
        canvas.rotate(-18f, c - 45f, c - 20f)
        canvas.drawOval(RectF(c - 75f, c - 50f, c - 15f, c + 15f), p)
        canvas.restore()
        canvas.save()
        canvas.rotate(18f, c + 45f, c - 20f)
        canvas.drawOval(RectF(c + 15f, c - 50f, c + 75f, c + 15f), p)
        canvas.restore()

        // Nose
        canvas.drawOval(RectF(c - 20f, c + 25f, c + 20f, c + 45f), p)
        // Smile
        p.style = Paint.Style.STROKE
        p.strokeWidth = 6f
        canvas.drawArc(RectF(c - 25f, c + 38f, c, c + 58f), 0f, 180f, false, p)
        canvas.drawArc(RectF(c, c + 38f, c + 25f, c + 58f), 0f, 180f, false, p)

        p.style = Paint.Style.FILL
        drawSparkleEyes(canvas, c - 45f, c - 20f, c + 45f, c - 20f)
        drawCheeks(canvas, c - 60f, c + 35f, c + 60f, c + 35f)
    }

    private fun drawTiger(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Ears
        p.color = Color.rgb(239, 108, 0)
        canvas.drawCircle(c - 80f, c - 80f, 35f, p)
        canvas.drawCircle(c + 80f, c - 80f, 35f, p)
        p.color = Color.rgb(255, 204, 128)
        canvas.drawCircle(c - 80f, c - 80f, 20f, p)
        canvas.drawCircle(c + 80f, c - 80f, 20f, p)

        // Head
        p.color = Color.rgb(245, 124, 0)
        canvas.drawCircle(c, c, 105f, p)

        // Tiger stripes
        p.color = Color.rgb(38, 50, 56)
        val sTop = Path().apply {
            moveTo(c - 12f, c - 95f); lineTo(c + 12f, c - 95f); lineTo(c, c - 55f); close()
        }
        val sL1 = Path().apply {
            moveTo(c - 105f, c - 20f); lineTo(c - 60f, c - 25f); lineTo(c - 105f, c - 35f); close()
        }
        val sL2 = Path().apply {
            moveTo(c - 105f, c + 15f); lineTo(c - 65f, c + 10f); lineTo(c - 105f, c); close()
        }
        val sR1 = Path().apply {
            moveTo(c + 105f, c - 20f); lineTo(c + 60f, c - 25f); lineTo(c + 105f, c - 35f); close()
        }
        val sR2 = Path().apply {
            moveTo(c + 105f, c + 15f); lineTo(c + 65f, c + 10f); lineTo(c + 105f, c); close()
        }
        canvas.drawPath(sTop, p); canvas.drawPath(sL1, p); canvas.drawPath(sL2, p); canvas.drawPath(sR1, p); canvas.drawPath(sR2, p)

        // Muzzle
        p.color = Color.WHITE
        canvas.drawOval(RectF(c - 55f, c + 15f, c + 55f, c + 75f), p)
        p.color = Color.rgb(62, 39, 35)
        canvas.drawCircle(c, c + 35f, 15f, p)

        drawEyes(canvas, c - 45f, c - 15f, c + 45f, c - 15f)
        drawCheeks(canvas, c - 60f, c + 30f, c + 60f, c + 30f)
    }

    private fun drawHippo(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(120, 144, 156)
        // Ears
        canvas.drawCircle(c - 85f, c - 80f, 25f, p)
        canvas.drawCircle(c + 85f, c - 80f, 25f, p)
        // Head
        canvas.drawCircle(c, c - 30f, 85f, p)
        // Big round snout
        p.color = Color.rgb(144, 164, 174)
        canvas.drawOval(RectF(c - 105f, c - 10f, c + 105f, c + 105f), p)

        // Nostrils
        p.color = Color.rgb(55, 71, 79)
        canvas.drawCircle(c - 40f, c + 35f, 15f, p)
        canvas.drawCircle(c + 40f, c + 35f, 15f, p)

        // Tiny teeth
        p.color = Color.WHITE
        canvas.drawRect(c - 30f, c + 85f, c - 15f, c + 105f, p)
        canvas.drawRect(c + 15f, c + 85f, c + 30f, c + 105f, p)

        drawEyes(canvas, c - 45f, c - 45f, c + 45f, c - 45f)
        drawCheeks(canvas, c - 75f, c + 20f, c + 75f, c + 20f)
    }

    private fun drawKangaroo(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(161, 136, 127)
        // Big tall ears
        canvas.drawOval(RectF(c - 85f, c - 145f, c - 35f, c - 65f), p)
        canvas.drawOval(RectF(c + 35f, c - 145f, c + 85f, c - 65f), p)
        p.color = Color.rgb(255, 204, 188)
        canvas.drawOval(RectF(c - 75f, c - 135f, c - 45f, c - 75f), p)
        canvas.drawOval(RectF(c + 45f, c - 135f, c + 75f, c - 75f), p)

        // Head
        p.color = Color.rgb(188, 170, 164)
        canvas.drawCircle(c, c - 20f, 80f, p)
        // Snout
        canvas.drawOval(RectF(c - 40f, c + 5f, c + 40f, c + 75f), p)
        p.color = Color.rgb(62, 39, 35)
        canvas.drawCircle(c, c + 30f, 14f, p)

        drawEyes(canvas, c - 35f, c - 30f, c + 35f, c - 30f)
        drawCheeks(canvas, c - 50f, c + 10f, c + 50f, c + 10f)
    }

    private fun drawBear(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(109, 76, 65)
        // Ears
        canvas.drawCircle(c - 85f, c - 80f, 35f, p)
        canvas.drawCircle(c + 85f, c - 80f, 35f, p)
        p.color = Color.rgb(188, 170, 164)
        canvas.drawCircle(c - 85f, c - 80f, 20f, p)
        canvas.drawCircle(c + 85f, c - 80f, 20f, p)

        // Head
        p.color = Color.rgb(121, 85, 72)
        canvas.drawCircle(c, c, 105f, p)
        // Snout
        p.color = Color.rgb(215, 204, 200)
        canvas.drawOval(RectF(c - 55f, c + 10f, c + 55f, c + 75f), p)
        p.color = Color.rgb(62, 39, 35)
        canvas.drawOval(RectF(c - 22f, c + 20f, c + 22f, c + 42f), p)

        drawEyes(canvas, c - 45f, c - 20f, c + 45f, c - 20f)
        drawCheeks(canvas, c - 60f, c + 25f, c + 60f, c + 25f)
    }

    private fun drawCrocodile(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(56, 142, 60)
        // Back ridges
        for (i in -3..3) {
            canvas.drawCircle(c + i * 25f, c - 70f, 18f, p)
        }
        // Big head
        canvas.drawOval(RectF(c - 120f, c - 30f, c + 120f, c + 70f), p)
        // Nostrils
        p.color = Color.rgb(27, 94, 32)
        canvas.drawCircle(c - 70f, c + 10f, 10f, p)
        canvas.drawCircle(c + 70f, c + 10f, 10f, p)

        // Sharp cute teeth
        p.color = Color.WHITE
        for (i in -3..3) {
            val tx = c + i * 30f
            val tooth = Path().apply {
                moveTo(tx - 10f, c + 40f); lineTo(tx + 10f, c + 40f); lineTo(tx, c + 60f); close()
            }
            canvas.drawPath(tooth, p)
        }

        // Bulging eyes
        p.color = Color.rgb(76, 175, 80)
        canvas.drawCircle(c - 50f, c - 45f, 32f, p)
        canvas.drawCircle(c + 50f, c - 45f, 32f, p)
        drawEyes(canvas, c - 50f, c - 45f, c + 50f, c - 45f)
        drawCheeks(canvas, c - 85f, c + 25f, c + 85f, c + 25f)
    }

    private fun drawPenguin(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Body
        p.color = Color.rgb(38, 50, 56)
        canvas.drawOval(RectF(c - 95f, c - 90f, c + 95f, c + 115f), p)
        // White belly
        p.color = Color.WHITE
        canvas.drawOval(RectF(c - 65f, c - 50f, c + 65f, c + 105f), p)
        // Flippers
        p.color = Color.rgb(38, 50, 56)
        canvas.drawOval(RectF(c - 120f, c - 20f, c - 70f, c + 70f), p)
        canvas.drawOval(RectF(c + 70f, c - 20f, c + 120f, c + 70f), p)
        // Orange feet
        p.color = Color.rgb(255, 143, 0)
        canvas.drawOval(RectF(c - 60f, c + 100f, c - 10f, c + 130f), p)
        canvas.drawOval(RectF(c + 10f, c + 100f, c + 60f, c + 130f), p)
        // Beak
        val beak = Path().apply {
            moveTo(c - 20f, c); lineTo(c + 20f, c); lineTo(c, c + 25f); close()
        }
        canvas.drawPath(beak, p)

        drawEyes(canvas, c - 35f, c - 30f, c + 35f, c - 30f)
        drawCheeks(canvas, c - 50f, c, c + 50f, c)
    }

    // --- FARM ANIMALS ---
    private fun drawFarmItem(canvas: Canvas, id: String, c: Float) {
        when (id) {
            "cow" -> drawCow(canvas, c)
            "horse" -> drawHorse(canvas, c)
            "sheep" -> drawSheep(canvas, c)
            "pig" -> drawPig(canvas, c)
            "chicken" -> drawChicken(canvas, c)
            "goat" -> drawGoat(canvas, c)
            "duck" -> drawDuck(canvas, c)
            "donkey" -> drawDonkey(canvas, c)
            "rabbit" -> drawRabbit(canvas, c)
            "dog" -> drawDog(canvas, c)
            "cat" -> drawCat(canvas, c)
            "rooster" -> drawRooster(canvas, c)
            else -> drawCuteCircleAnimal(canvas, c, Color.rgb(255, 183, 77), id)
        }
    }

    private fun drawCow(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Horns
        p.color = Color.rgb(255, 213, 79)
        canvas.drawOval(RectF(c - 75f, c - 110f, c - 45f, c - 60f), p)
        canvas.drawOval(RectF(c + 45f, c - 110f, c + 75f, c - 60f), p)
        // Ears
        p.color = Color.rgb(255, 204, 188)
        canvas.drawOval(RectF(c - 105f, c - 60f, c - 60f, c - 20f), p)
        canvas.drawOval(RectF(c + 60f, c - 60f, c + 105f, c - 20f), p)

        // Head
        p.color = Color.WHITE
        canvas.drawCircle(c, c - 10f, 90f, p)
        // Black patches
        p.color = Color.rgb(45, 45, 45)
        canvas.drawCircle(c - 45f, c - 45f, 32f, p)
        canvas.drawCircle(c + 55f, c - 15f, 25f, p)

        // Pink Snout
        p.color = Color.rgb(248, 187, 208)
        canvas.drawOval(RectF(c - 65f, c + 15f, c + 65f, c + 85f), p)
        p.color = Color.rgb(194, 24, 91)
        canvas.drawCircle(c - 25f, c + 48f, 10f, p)
        canvas.drawCircle(c + 25f, c + 48f, 10f, p)

        drawEyes(canvas, c - 35f, c - 25f, c + 35f, c - 25f)
        drawCheeks(canvas, c - 55f, c + 10f, c + 55f, c + 10f)
    }

    private fun drawPig(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(244, 143, 177)
        // Ears
        val e1 = Path().apply { moveTo(c - 85f, c - 85f); lineTo(c - 35f, c - 85f); lineTo(c - 60f, c - 135f); close() }
        val e2 = Path().apply { moveTo(c + 35f, c - 85f); lineTo(c + 85f, c - 85f); lineTo(c + 60f, c - 135f); close() }
        canvas.drawPath(e1, p); canvas.drawPath(e2, p)

        // Head
        p.color = Color.rgb(248, 187, 208)
        canvas.drawCircle(c, c, 100f, p)

        // Big round snout
        p.color = Color.rgb(240, 98, 146)
        canvas.drawOval(RectF(c - 50f, c + 10f, c + 50f, c + 65f), p)
        p.color = Color.rgb(136, 14, 79)
        canvas.drawCircle(c - 20f, c + 37f, 10f, p)
        canvas.drawCircle(c + 20f, c + 37f, 10f, p)

        drawEyes(canvas, c - 45f, c - 25f, c + 45f, c - 25f)
        drawCheeks(canvas, c - 60f, c + 25f, c + 60f, c + 25f)
    }

    private fun drawSheep(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Fluffy cloud wool
        p.color = Color.rgb(238, 238, 238)
        for (i in 0 until 12) {
            val a = i * (Math.PI * 2 / 12)
            canvas.drawCircle(c + (95f * Math.cos(a)).toFloat(), c + (95f * Math.sin(a)).toFloat(), 45f, p)
        }
        canvas.drawCircle(c, c, 95f, p)

        // Head
        p.color = Color.rgb(55, 71, 79)
        canvas.drawOval(RectF(c - 55f, c - 45f, c + 55f, c + 55f), p)

        // Ears
        canvas.drawOval(RectF(c - 90f, c - 40f, c - 40f, c - 15f), p)
        canvas.drawOval(RectF(c + 40f, c - 40f, c + 90f, c - 15f), p)

        // Wool puff on top of head
        p.color = Color.rgb(238, 238, 238)
        canvas.drawCircle(c, c - 45f, 25f, p)

        drawSparkleEyes(canvas, c - 25f, c - 10f, c + 25f, c - 10f)
        drawCheeks(canvas, c - 38f, c + 18f, c + 38f, c + 18f)
    }

    private fun drawHorse(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Mane
        p.color = Color.rgb(62, 39, 35)
        canvas.drawRect(c - 20f, c - 130f, c + 20f, c - 60f, p)

        // Ears
        p.color = Color.rgb(141, 110, 99)
        canvas.drawOval(RectF(c - 65f, c - 125f, c - 35f, c - 70f), p)
        canvas.drawOval(RectF(c + 35f, c - 125f, c + 65f, c - 70f), p)

        // Head
        canvas.drawOval(RectF(c - 65f, c - 80f, c + 65f, c + 50f), p)
        // Snout
        p.color = Color.rgb(188, 170, 164)
        canvas.drawOval(RectF(c - 50f, c + 15f, c + 50f, c + 85f), p)
        p.color = Color.rgb(62, 39, 35)
        canvas.drawCircle(c - 20f, c + 50f, 8f, p)
        canvas.drawCircle(c + 20f, c + 50f, 8f, p)

        drawEyes(canvas, c - 35f, c - 35f, c + 35f, c - 35f)
        drawCheeks(canvas, c - 50f, c, c + 50f, c)
    }

    private fun drawChicken(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Comb
        p.color = Color.rgb(229, 57, 53)
        canvas.drawCircle(c - 25f, c - 95f, 20f, p)
        canvas.drawCircle(c, c - 105f, 24f, p)
        canvas.drawCircle(c + 25f, c - 95f, 20f, p)

        // Head & Body
        p.color = Color.rgb(255, 238, 88)
        canvas.drawCircle(c, c, 100f, p)

        // Wings
        p.color = Color.rgb(253, 216, 53)
        canvas.drawOval(RectF(c - 110f, c, c - 60f, c + 70f), p)
        canvas.drawOval(RectF(c + 60f, c, c + 110f, c + 70f), p)

        // Wattle
        p.color = Color.rgb(229, 57, 53)
        canvas.drawCircle(c, c + 45f, 15f, p)

        // Beak
        p.color = Color.rgb(255, 143, 0)
        val beak = Path().apply {
            moveTo(c - 25f, c + 10f); lineTo(c + 25f, c + 10f); lineTo(c, c + 35f); close()
        }
        canvas.drawPath(beak, p)

        drawEyes(canvas, c - 40f, c - 20f, c + 40f, c - 20f)
        drawCheeks(canvas, c - 55f, c + 15f, c + 55f, c + 15f)
    }

    private fun drawGoat(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Horns
        p.color = Color.rgb(189, 189, 189)
        canvas.drawOval(RectF(c - 70f, c - 130f, c - 40f, c - 60f), p)
        canvas.drawOval(RectF(c + 40f, c - 130f, c + 70f, c - 60f), p)

        // Head
        p.color = Color.rgb(238, 238, 238)
        canvas.drawOval(RectF(c - 65f, c - 75f, c + 65f, c + 55f), p)
        // Ears floppy
        canvas.drawOval(RectF(c - 105f, c - 30f, c - 55f, c + 5f), p)
        canvas.drawOval(RectF(c + 55f, c - 30f, c + 105f, c + 5f), p)

        // Beard
        val beard = Path().apply {
            moveTo(c - 20f, c + 55f); lineTo(c + 20f, c + 55f); lineTo(c, c + 95f); close()
        }
        canvas.drawPath(beard, p)

        // Snout
        p.color = Color.rgb(255, 204, 188)
        canvas.drawOval(RectF(c - 40f, c + 15f, c + 40f, c + 65f), p)
        p.color = Color.rgb(66, 66, 66)
        canvas.drawCircle(c - 15f, c + 38f, 6f, p)
        canvas.drawCircle(c + 15f, c + 38f, 6f, p)

        drawEyes(canvas, c - 35f, c - 25f, c + 35f, c - 25f)
        drawCheeks(canvas, c - 45f, c + 10f, c + 45f, c + 10f)
    }

    private fun drawDuck(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(255, 235, 59)
        canvas.drawCircle(c, c - 10f, 95f, p)

        // Big round orange bill
        p.color = Color.rgb(255, 152, 0)
        canvas.drawOval(RectF(c - 55f, c + 15f, c + 55f, c + 75f), p)
        p.color = Color.rgb(230, 81, 0)
        canvas.drawCircle(c - 15f, c + 35f, 6f, p)
        canvas.drawCircle(c + 15f, c + 35f, 6f, p)

        drawEyes(canvas, c - 35f, c - 25f, c + 35f, c - 25f)
        drawCheeks(canvas, c - 55f, c + 10f, c + 55f, c + 10f)
    }

    private fun drawDonkey(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(158, 158, 158)
        // Very tall donkey ears
        canvas.drawOval(RectF(c - 75f, c - 155f, c - 25f, c - 60f), p)
        canvas.drawOval(RectF(c + 25f, c - 155f, c + 75f, c - 60f), p)
        p.color = Color.rgb(255, 204, 188)
        canvas.drawOval(RectF(c - 65f, c - 145f, c - 35f, c - 70f), p)
        canvas.drawOval(RectF(c + 35f, c - 145f, c + 65f, c - 70f), p)

        // Head
        p.color = Color.rgb(158, 158, 158)
        canvas.drawCircle(c, c - 10f, 85f, p)
        // White muzzle
        p.color = Color.WHITE
        canvas.drawOval(RectF(c - 55f, c + 15f, c + 55f, c + 85f), p)
        p.color = Color.rgb(66, 66, 66)
        canvas.drawCircle(c - 20f, c + 45f, 9f, p)
        canvas.drawCircle(c + 20f, c + 45f, 9f, p)

        drawEyes(canvas, c - 35f, c - 30f, c + 35f, c - 30f)
        drawCheeks(canvas, c - 50f, c + 5f, c + 50f, c + 5f)
    }

    private fun drawRabbit(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Long pink ears
        p.color = Color.WHITE
        canvas.drawOval(RectF(c - 65f, c - 165f, c - 20f, c - 60f), p)
        canvas.drawOval(RectF(c + 20f, c - 165f, c + 65f, c - 60f), p)
        p.color = Color.rgb(248, 187, 208)
        canvas.drawOval(RectF(c - 55f, c - 155f, c - 30f, c - 70f), p)
        canvas.drawOval(RectF(c + 30f, c - 155f, c + 55f, c - 70f), p)

        // Head
        p.color = Color.WHITE
        canvas.drawCircle(c, c, 95f, p)

        // Nose
        p.color = Color.rgb(240, 98, 146)
        canvas.drawOval(RectF(c - 16f, c + 20f, c + 16f, c + 38f), p)

        // Whiskers
        p.color = Color.rgb(189, 189, 189)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 4f
        canvas.drawLine(c - 70f, c + 25f, c - 25f, c + 30f, p)
        canvas.drawLine(c - 70f, c + 40f, c - 25f, c + 35f, p)
        canvas.drawLine(c + 25f, c + 30f, c + 70f, c + 25f, p)
        canvas.drawLine(c + 25f, c + 35f, c + 70f, c + 40f, p)

        p.style = Paint.Style.FILL
        drawEyes(canvas, c - 40f, c - 15f, c + 40f, c - 15f)
        drawCheeks(canvas, c - 55f, c + 25f, c + 55f, c + 25f)
    }

    private fun drawDog(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Floppy brown ears
        p.color = Color.rgb(141, 110, 99)
        canvas.drawOval(RectF(c - 110f, c - 60f, c - 50f, c + 50f), p)
        canvas.drawOval(RectF(c + 50f, c - 60f, c + 110f, c + 50f), p)

        // Head
        p.color = Color.rgb(215, 204, 200)
        canvas.drawCircle(c, c, 95f, p)

        // Snout
        p.color = Color.WHITE
        canvas.drawOval(RectF(c - 50f, c + 10f, c + 50f, c + 75f), p)
        p.color = Color.rgb(62, 39, 35)
        canvas.drawOval(RectF(c - 22f, c + 18f, c + 22f, c + 40f), p)

        // Cute tongue
        p.color = Color.rgb(239, 83, 80)
        canvas.drawOval(RectF(c - 15f, c + 55f, c + 15f, c + 85f), p)

        drawEyes(canvas, c - 40f, c - 20f, c + 40f, c - 20f)
        drawCheeks(canvas, c - 55f, c + 20f, c + 55f, c + 20f)
    }

    private fun drawCat(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(255, 167, 38)
        // Pointy ears
        val e1 = Path().apply { moveTo(c - 85f, c - 40f); lineTo(c - 40f, c - 85f); lineTo(c - 75f, c - 125f); close() }
        val e2 = Path().apply { moveTo(c + 85f, c - 40f); lineTo(c + 40f, c - 85f); lineTo(c + 75f, c - 125f); close() }
        canvas.drawPath(e1, p); canvas.drawPath(e2, p)

        // Head
        canvas.drawCircle(c, c, 95f, p)

        // Muzzle
        p.color = Color.WHITE
        canvas.drawCircle(c - 22f, c + 35f, 25f, p)
        canvas.drawCircle(c + 22f, c + 35f, 25f, p)

        // Nose
        p.color = Color.rgb(240, 98, 146)
        canvas.drawOval(RectF(c - 15f, c + 15f, c + 15f, c + 32f), p)

        // Whiskers
        p.color = Color.rgb(66, 66, 66)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 4f
        canvas.drawLine(c - 75f, c + 25f, c - 30f, c + 32f, p)
        canvas.drawLine(c - 75f, c + 42f, c - 30f, c + 38f, p)
        canvas.drawLine(c + 30f, c + 32f, c + 75f, c + 25f, p)
        canvas.drawLine(c + 30f, c + 38f, c + 75f, c + 42f, p)

        p.style = Paint.Style.FILL
        drawEyes(canvas, c - 40f, c - 20f, c + 40f, c - 20f)
        drawCheeks(canvas, c - 55f, c + 15f, c + 55f, c + 15f)
    }

    private fun drawRooster(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Big red comb
        p.color = Color.rgb(211, 47, 47)
        canvas.drawCircle(c - 30f, c - 105f, 25f, p)
        canvas.drawCircle(c, c - 120f, 30f, p)
        canvas.drawCircle(c + 30f, c - 105f, 25f, p)

        // White head
        p.color = Color.WHITE
        canvas.drawCircle(c, c - 10f, 90f, p)

        // Wattle
        p.color = Color.rgb(211, 47, 47)
        canvas.drawOval(RectF(c - 15f, c + 35f, c + 15f, c + 80f), p)

        // Yellow beak
        p.color = Color.rgb(255, 179, 0)
        val beak = Path().apply {
            moveTo(c - 20f, c + 10f); lineTo(c + 20f, c + 10f); lineTo(c, c + 35f); close()
        }
        canvas.drawPath(beak, p)

        drawEyes(canvas, c - 35f, c - 25f, c + 35f, c - 25f)
        drawCheeks(canvas, c - 50f, c + 15f, c + 50f, c + 15f)
    }

    // --- SEA ANIMALS ---
    private fun drawSeaItem(canvas: Canvas, id: String, c: Float) {
        when (id) {
            "dolphin" -> drawDolphin(canvas, c)
            "shark" -> drawShark(canvas, c)
            "octopus" -> drawOctopus(canvas, c)
            "turtle" -> drawTurtle(canvas, c)
            "crab" -> drawCrab(canvas, c)
            "whale" -> drawWhale(canvas, c)
            "seahorse" -> drawSeahorse(canvas, c)
            "starfish" -> drawStarfish(canvas, c)
            "jellyfish" -> drawJellyfish(canvas, c)
            "clownfish" -> drawClownfish(canvas, c)
            "seal" -> drawSeal(canvas, c)
            "lobster" -> drawLobster(canvas, c)
            else -> drawCuteCircleAnimal(canvas, c, Color.rgb(33, 150, 243), id)
        }
    }

    private fun drawDolphin(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(41, 182, 246)
        // Dorsal fin
        val fin = Path().apply { moveTo(c - 20f, c - 70f); quadTo(c, c - 120f, c + 40f, c - 70f); close() }
        canvas.drawPath(fin, p)

        // Arched body
        canvas.drawOval(RectF(c - 120f, c - 60f, c + 120f, c + 50f), p)

        // Tail
        val tail = Path().apply {
            moveTo(c - 110f, c); lineTo(c - 150f, c - 40f); lineTo(c - 135f, c); lineTo(c - 150f, c + 40f); close()
        }
        canvas.drawPath(tail, p)

        // Belly
        p.color = Color.WHITE
        canvas.drawOval(RectF(c - 80f, c, c + 90f, c + 45f), p)

        // Snout
        p.color = Color.rgb(41, 182, 246)
        canvas.drawOval(RectF(c + 85f, c - 20f, c + 145f, c + 15f), p)

        drawEyes(canvas, c + 45f, c - 25f, c + 45f, c - 25f)
        drawCheeks(canvas, c + 30f, c, c + 30f, c)
    }

    private fun drawShark(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(120, 144, 156)
        // Big dorsal fin
        val fin = Path().apply { moveTo(c - 30f, c - 50f); lineTo(c - 10f, c - 125f); lineTo(c + 45f, c - 50f); close() }
        canvas.drawPath(fin, p)

        // Shark body
        canvas.drawOval(RectF(c - 130f, c - 60f, c + 130f, c + 60f), p)
        // White belly
        p.color = Color.WHITE
        canvas.drawOval(RectF(c - 90f, c + 5f, c + 110f, c + 55f), p)

        // Cute teeth
        for (i in 0..4) {
            val t = Path().apply {
                val tx = c + 40f + i * 15f
                moveTo(tx - 6f, c + 25f); lineTo(tx + 6f, c + 25f); lineTo(tx, c + 38f); close()
            }
            canvas.drawPath(t, p)
        }

        drawEyes(canvas, c + 55f, c - 20f, c + 55f, c - 20f)
        drawCheeks(canvas, c + 35f, c + 10f, c + 35f, c + 10f)
    }

    private fun drawOctopus(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(171, 71, 188)
        // Tentacles
        for (i in 0 until 6) {
            val tx = c - 90f + i * 36f
            canvas.drawOval(RectF(tx - 18f, c + 30f, tx + 18f, c + 115f), p)
            p.color = Color.rgb(248, 187, 208)
            canvas.drawCircle(tx, c + 95f, 8f, p)
            p.color = Color.rgb(171, 71, 188)
        }
        // Big bulbous head
        canvas.drawOval(RectF(c - 100f, c - 105f, c + 100f, c + 55f), p)

        drawSparkleEyes(canvas, c - 45f, c - 15f, c + 45f, c - 15f)
        drawCheeks(canvas, c - 65f, c + 20f, c + 65f, c + 20f)
    }

    private fun drawTurtle(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Green flippers
        p.color = Color.rgb(102, 187, 106)
        canvas.drawOval(RectF(c - 125f, c - 85f, c - 65f, c - 25f), p)
        canvas.drawOval(RectF(c + 65f, c - 85f, c + 125f, c - 25f), p)
        canvas.drawOval(RectF(c - 110f, c + 45f, c - 60f, c + 95f), p)
        canvas.drawOval(RectF(c + 60f, c + 45f, c + 110f, c + 95f), p)

        // Head
        canvas.drawOval(RectF(c - 35f, c - 125f, c + 35f, c - 65f), p)

        // Shell
        p.color = Color.rgb(46, 125, 50)
        canvas.drawOval(RectF(c - 95f, c - 65f, c + 95f, c + 85f), p)

        // Shell hex pattern
        p.color = Color.rgb(129, 199, 132)
        canvas.drawCircle(c, c + 10f, 32f, p)
        canvas.drawCircle(c - 45f, c - 15f, 22f, p)
        canvas.drawCircle(c + 45f, c - 15f, 22f, p)
        canvas.drawCircle(c - 45f, c + 35f, 22f, p)
        canvas.drawCircle(c + 45f, c + 35f, 22f, p)

        drawEyes(canvas, c - 18f, c - 100f, c + 18f, c - 100f)
    }

    private fun drawCrab(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(239, 83, 80)
        // Legs
        for (i in -2..2) {
            canvas.drawOval(RectF(c - 120f + i * 15f, c + 40f, c - 80f + i * 15f, c + 95f), p)
            canvas.drawOval(RectF(c + 80f + i * 15f, c + 40f, c + 120f + i * 15f, c + 95f), p)
        }
        // Big claws
        canvas.drawCircle(c - 95f, c - 50f, 38f, p)
        canvas.drawCircle(c + 95f, c - 50f, 38f, p)
        p.color = Color.WHITE
        canvas.drawOval(RectF(c - 105f, c - 70f, c - 85f, c - 30f), p)
        canvas.drawOval(RectF(c + 85f, c - 70f, c + 105f, c - 30f), p)

        // Body
        p.color = Color.rgb(229, 57, 53)
        canvas.drawOval(RectF(c - 95f, c - 45f, c + 95f, c + 65f), p)

        // Stalk eyes
        p.color = Color.rgb(229, 57, 53)
        canvas.drawRect(c - 45f, c - 85f, c - 25f, c - 45f, p)
        canvas.drawRect(c + 25f, c - 85f, c + 45f, c - 45f, p)
        drawEyes(canvas, c - 35f, c - 90f, c + 35f, c - 90f)
        drawCheeks(canvas, c - 55f, c + 20f, c + 55f, c + 20f)
    }

    private fun drawWhale(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(30, 136, 229)
        // Water spout
        p.color = Color.rgb(129, 212, 250)
        canvas.drawCircle(c + 15f, c - 110f, 15f, p)
        canvas.drawCircle(c + 40f, c - 125f, 18f, p)
        canvas.drawCircle(c - 10f, c - 125f, 18f, p)

        // Body
        p.color = Color.rgb(30, 136, 229)
        canvas.drawOval(RectF(c - 130f, c - 70f, c + 130f, c + 70f), p)
        // Tail fin
        val tail = Path().apply {
            moveTo(c - 120f, c); lineTo(c - 165f, c - 45f); lineTo(c - 145f, c); lineTo(c - 165f, c + 45f); close()
        }
        canvas.drawPath(tail, p)

        // White belly
        p.color = Color.WHITE
        canvas.drawOval(RectF(c - 70f, c + 20f, c + 115f, c + 68f), p)

        drawEyes(canvas, c + 65f, c - 15f, c + 65f, c - 15f)
        drawCheeks(canvas, c + 45f, c + 15f, c + 45f, c + 15f)
    }

    private fun drawSeahorse(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(255, 179, 0)
        // Head
        canvas.drawCircle(c, c - 75f, 45f, p)
        // Snout
        canvas.drawRect(c + 20f, c - 85f, c + 65f, c - 65f, p)
        // Crown ridges
        for (i in 0..3) {
            canvas.drawCircle(c - 25f + i * 15f, c - 115f, 12f, p)
        }
        // Curled body
        canvas.drawOval(RectF(c - 40f, c - 40f, c + 40f, c + 50f), p)
        // Curled tail
        p.style = Paint.Style.STROKE
        p.strokeWidth = 24f
        canvas.drawArc(RectF(c - 60f, c + 20f, c + 20f, c + 115f), 45f, 240f, false, p)
        p.style = Paint.Style.FILL

        drawEyes(canvas, c + 5f, c - 85f, c + 5f, c - 85f)
        drawCheeks(canvas, c, c - 65f, c, c - 65f)
    }

    private fun drawStarfish(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(255, 112, 67)
        val path = Path()
        val numPoints = 5
        val outerRadius = 135f
        val innerRadius = 60f
        for (i in 0 until numPoints * 2) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val angle = i * Math.PI / numPoints - Math.PI / 2
            val x = c + (r * Math.cos(angle)).toFloat()
            val y = c + (r * Math.sin(angle)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, p)

        // Texture dots
        p.color = Color.rgb(255, 204, 188)
        for (i in 0 until 5) {
            val a = i * (Math.PI * 2 / 5) - Math.PI / 2
            canvas.drawCircle(c + (95f * Math.cos(a)).toFloat(), c + (95f * Math.sin(a)).toFloat(), 8f, p)
        }

        drawSparkleEyes(canvas, c - 28f, c - 10f, c + 28f, c - 10f)
        drawCheeks(canvas, c - 45f, c + 15f, c + 45f, c + 15f)
    }

    private fun drawJellyfish(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(240, 98, 146)
        // Umbrella bell
        canvas.drawArc(RectF(c - 95f, c - 95f, c + 95f, c + 45f), 180f, 180f, true, p)
        p.color = Color.rgb(248, 187, 208)
        canvas.drawOval(RectF(c - 95f, c - 5f, c + 95f, c + 35f), p)

        // Flowing tentacles
        p.color = Color.rgb(240, 98, 146)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 8f
        for (i in -3..3) {
            val tx = c + i * 22f
            val tentacle = Path().apply {
                moveTo(tx, c + 25f)
                quadTo(tx + 20f, c + 65f, tx - 10f, c + 95f)
                quadTo(tx + 10f, c + 120f, tx, c + 135f)
            }
            canvas.drawPath(tentacle, p)
        }
        p.style = Paint.Style.FILL

        drawEyes(canvas, c - 38f, c - 45f, c + 38f, c - 45f)
        drawCheeks(canvas, c - 55f, c - 15f, c + 55f, c - 15f)
    }

    private fun drawClownfish(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(255, 109, 0)
        // Body
        canvas.drawOval(RectF(c - 110f, c - 65f, c + 110f, c + 65f), p)
        // Tail fin
        val tail = Path().apply {
            moveTo(c - 95f, c); lineTo(c - 145f, c - 40f); lineTo(c - 130f, c); lineTo(c - 145f, c + 40f); close()
        }
        canvas.drawPath(tail, p)

        // White stripes with black borders
        p.color = Color.WHITE
        canvas.drawRect(c - 20f, c - 60f, c + 5f, c + 60f, p)
        canvas.drawRect(c + 45f, c - 45f, c + 65f, c + 45f, p)
        p.color = Color.rgb(33, 33, 33)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 5f
        canvas.drawRect(c - 20f, c - 60f, c + 5f, c + 60f, p)
        canvas.drawRect(c + 45f, c - 45f, c + 65f, c + 45f, p)
        p.style = Paint.Style.FILL

        drawEyes(canvas, c + 65f, c - 15f, c + 65f, c - 15f)
        drawCheeks(canvas, c + 45f, c + 15f, c + 45f, c + 15f)
    }

    private fun drawSeal(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(144, 164, 174)
        // Body
        canvas.drawOval(RectF(c - 95f, c - 70f, c + 95f, c + 85f), p)
        // Flippers
        canvas.drawOval(RectF(c - 125f, c + 15f, c - 65f, c + 65f), p)
        canvas.drawOval(RectF(c + 65f, c + 15f, c + 125f, c + 65f), p)

        // White muzzle
        p.color = Color.WHITE
        canvas.drawOval(RectF(c - 45f, c + 15f, c + 45f, c + 65f), p)
        p.color = Color.rgb(38, 50, 56)
        canvas.drawCircle(c, c + 30f, 12f, p)

        // Whiskers
        p.style = Paint.Style.STROKE
        p.strokeWidth = 4f
        canvas.drawLine(c - 55f, c + 35f, c - 20f, c + 38f, p)
        canvas.drawLine(c + 20f, c + 38f, c + 55f, c + 35f, p)
        p.style = Paint.Style.FILL

        drawEyes(canvas, c - 38f, c - 15f, c + 38f, c - 15f)
        drawCheeks(canvas, c - 55f, c + 20f, c + 55f, c + 20f)
    }

    private fun drawLobster(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(198, 40, 40)
        // Big claws
        canvas.drawCircle(c - 85f, c - 65f, 40f, p)
        canvas.drawCircle(c + 85f, c - 65f, 40f, p)
        p.color = Color.WHITE
        canvas.drawOval(RectF(c - 95f, c - 85f, c - 75f, c - 45f), p)
        canvas.drawOval(RectF(c + 75f, c - 85f, c + 95f, c - 45f), p)

        // Body segments
        p.color = Color.rgb(211, 47, 47)
        canvas.drawOval(RectF(c - 65f, c - 50f, c + 65f, c + 30f), p)
        canvas.drawOval(RectF(c - 50f, c + 15f, c + 50f, c + 65f), p)
        canvas.drawOval(RectF(c - 40f, c + 55f, c + 40f, c + 95f), p)

        // Tail fan
        val tail = Path().apply {
            moveTo(c - 45f, c + 125f); lineTo(c + 45f, c + 125f); lineTo(c, c + 90f); close()
        }
        canvas.drawPath(tail, p)

        drawEyes(canvas, c - 30f, c - 40f, c + 30f, c - 40f)
        drawCheeks(canvas, c - 45f, c, c + 45f, c)
    }

    // --- BIRDS ---
    private fun drawBirdItem(canvas: Canvas, id: String, c: Float) {
        when (id) {
            "parrot" -> drawParrot(canvas, c)
            "sparrow" -> drawSparrow(canvas, c)
            "owl" -> drawOwl(canvas, c)
            "peacock" -> drawPeacock(canvas, c)
            "eagle" -> drawEagle(canvas, c)
            "pigeon" -> drawPigeon(canvas, c)
            "duck_bird" -> drawDuck(canvas, c)
            "flamingo" -> drawFlamingo(canvas, c)
            "toucan" -> drawToucan(canvas, c)
            "woodpecker" -> drawWoodpecker(canvas, c)
            "hummingbird" -> drawHummingbird(canvas, c)
            "crow" -> drawCrow(canvas, c)
            else -> drawCuteCircleAnimal(canvas, c, Color.rgb(156, 39, 176), id)
        }
    }

    private fun drawParrot(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Red body
        p.color = Color.rgb(229, 57, 53)
        canvas.drawOval(RectF(c - 70f, c - 95f, c + 70f, c + 95f), p)

        // Multi-colored wing (blue, green, yellow)
        p.color = Color.rgb(30, 136, 229)
        canvas.drawOval(RectF(c - 95f, c - 15f, c - 15f, c + 75f), p)
        p.color = Color.rgb(67, 160, 71)
        canvas.drawOval(RectF(c - 85f, c + 10f, c - 20f, c + 80f), p)
        p.color = Color.rgb(253, 216, 53)
        canvas.drawOval(RectF(c - 75f, c + 35f, c - 25f, c + 85f), p)

        // Curved beak
        p.color = Color.rgb(255, 179, 0)
        val beak = Path().apply {
            moveTo(c + 45f, c - 45f); quadTo(c + 115f, c - 30f, c + 75f, c + 20f); lineTo(c + 45f, c); close()
        }
        canvas.drawPath(beak, p)

        drawEyes(canvas, c + 25f, c - 45f, c + 25f, c - 45f)
        drawCheeks(canvas, c + 15f, c - 15f, c + 15f, c - 15f)
    }

    private fun drawOwl(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(121, 85, 72)
        // Tuft ears
        val e1 = Path().apply { moveTo(c - 75f, c - 85f); lineTo(c - 35f, c - 95f); lineTo(c - 65f, c - 145f); close() }
        val e2 = Path().apply { moveTo(c + 75f, c - 85f); lineTo(c + 35f, c - 95f); lineTo(c + 65f, c - 145f); close() }
        canvas.drawPath(e1, p); canvas.drawPath(e2, p)

        // Head and Body
        canvas.drawOval(RectF(c - 95f, c - 85f, c + 95f, c + 105f), p)

        // Big round eye rings
        p.color = Color.WHITE
        canvas.drawCircle(c - 45f, c - 20f, 42f, p)
        canvas.drawCircle(c + 45f, c - 20f, 42f, p)

        // Golden eyes with big pupils
        p.color = Color.rgb(255, 193, 7)
        canvas.drawCircle(c - 45f, c - 20f, 30f, p)
        canvas.drawCircle(c + 45f, c - 20f, 30f, p)
        p.color = Color.rgb(33, 33, 33)
        canvas.drawCircle(c - 45f, c - 20f, 18f, p)
        canvas.drawCircle(c + 45f, c - 20f, 18f, p)
        p.color = Color.WHITE
        canvas.drawCircle(c - 50f, c - 25f, 6f, p)
        canvas.drawCircle(c + 40f, c - 25f, 6f, p)

        // Beak
        p.color = Color.rgb(255, 152, 0)
        val beak = Path().apply {
            moveTo(c - 15f, c - 10f); lineTo(c + 15f, c - 10f); lineTo(c, c + 25f); close()
        }
        canvas.drawPath(beak, p)

        // Feather breast scallops
        p.color = Color.rgb(215, 204, 200)
        canvas.drawOval(RectF(c - 50f, c + 25f, c + 50f, c + 85f), p)
    }

    private fun drawPeacock(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Peacock tail fan
        val colors = listOf(Color.rgb(0, 150, 136), Color.rgb(3, 169, 244), Color.rgb(76, 175, 80))
        for (i in 0 until 9) {
            val angle = Math.PI + i * (Math.PI / 8)
            val fx = c + (130f * Math.cos(angle)).toFloat()
            val fy = c + 40f + (130f * Math.sin(angle)).toFloat()
            p.color = colors[i % colors.size]
            canvas.drawCircle(fx, fy, 28f, p)
            p.color = Color.rgb(255, 235, 59)
            canvas.drawCircle(fx, fy, 14f, p)
            p.color = Color.rgb(30, 136, 229)
            canvas.drawCircle(fx, fy, 7f, p)
        }

        // Royal blue body
        p.color = Color.rgb(13, 71, 161)
        canvas.drawOval(RectF(c - 40f, c - 50f, c + 40f, c + 75f), p)
        canvas.drawCircle(c, c - 60f, 35f, p)

        // Crest
        p.color = Color.rgb(3, 169, 244)
        canvas.drawCircle(c - 15f, c - 105f, 8f, p)
        canvas.drawCircle(c, c - 110f, 10f, p)
        canvas.drawCircle(c + 15f, c - 105f, 8f, p)

        // Beak
        p.color = Color.rgb(255, 179, 0)
        val beak = Path().apply { moveTo(c - 12f, c - 55f); lineTo(c + 12f, c - 55f); lineTo(c, c - 35f); close() }
        canvas.drawPath(beak, p)

        drawEyes(canvas, c - 18f, c - 70f, c + 18f, c - 70f)
        drawCheeks(canvas, c - 28f, c - 50f, c + 28f, c - 50f)
    }

    private fun drawFlamingo(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(240, 98, 146)
        // Curved neck
        p.style = Paint.Style.STROKE
        p.strokeWidth = 32f
        val neck = Path().apply {
            moveTo(c - 20f, c + 40f)
            quadTo(c - 60f, c - 50f, c + 20f, c - 80f)
        }
        canvas.drawPath(neck, p)
        p.style = Paint.Style.FILL

        // Head
        canvas.drawCircle(c + 20f, c - 80f, 32f, p)
        // Body
        canvas.drawOval(RectF(c - 70f, c + 10f, c + 50f, c + 95f), p)

        // Beak with black tip
        p.color = Color.rgb(255, 204, 188)
        val b1 = Path().apply { moveTo(c + 40f, c - 88f); lineTo(c + 85f, c - 80f); lineTo(c + 55f, c - 60f); close() }
        canvas.drawPath(b1, p)
        p.color = Color.rgb(33, 33, 33)
        val b2 = Path().apply { moveTo(c + 65f, c - 84f); lineTo(c + 85f, c - 80f); lineTo(c + 72f, c - 68f); close() }
        canvas.drawPath(b2, p)

        drawEyes(canvas, c + 25f, c - 90f, c + 25f, c - 90f)
        drawCheeks(canvas, c + 15f, c - 70f, c + 15f, c - 70f)
    }

    private fun drawToucan(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Black body
        p.color = Color.rgb(38, 50, 56)
        canvas.drawOval(RectF(c - 85f, c - 80f, c + 45f, c + 95f), p)
        // White throat
        p.color = Color.WHITE
        canvas.drawCircle(c - 20f, c - 30f, 45f, p)

        // Giant rainbow beak
        p.color = Color.rgb(255, 179, 0)
        val beak = Path().apply {
            moveTo(c + 15f, c - 65f)
            quadTo(c + 145f, c - 50f, c + 130f, c + 25f)
            lineTo(c + 15f, c - 5f)
            close()
        }
        canvas.drawPath(beak, p)
        // Orange/Red tip
        p.color = Color.rgb(229, 57, 53)
        canvas.drawCircle(c + 115f, c - 15f, 22f, p)

        drawEyes(canvas, c - 20f, c - 45f, c - 20f, c - 45f)
        drawCheeks(canvas, c - 35f, c - 15f, c - 35f, c - 15f)
    }

    private fun drawEagle(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(78, 52, 46)
        // Brown body
        canvas.drawOval(RectF(c - 80f, c - 20f, c + 80f, c + 110f), p)

        // White head
        p.color = Color.WHITE
        canvas.drawOval(RectF(c - 70f, c - 95f, c + 70f, c + 15f), p)

        // Hooked golden beak
        p.color = Color.rgb(255, 179, 0)
        val beak = Path().apply {
            moveTo(c - 25f, c - 25f); lineTo(c + 25f, c - 25f); lineTo(c, c + 25f); close()
        }
        canvas.drawPath(beak, p)

        drawEyes(canvas, c - 38f, c - 45f, c + 38f, c - 45f)
        drawCheeks(canvas, c - 50f, c - 10f, c + 50f, c - 10f)
    }

    private fun drawSparrow(canvas: Canvas, c: Float) = drawGenericBird(canvas, c, Color.rgb(141, 110, 99))
    private fun drawPigeon(canvas: Canvas, c: Float) = drawGenericBird(canvas, c, Color.rgb(176, 190, 197))
    private fun drawWoodpecker(canvas: Canvas, c: Float) = drawGenericBird(canvas, c, Color.rgb(229, 57, 53))
    private fun drawHummingbird(canvas: Canvas, c: Float) = drawGenericBird(canvas, c, Color.rgb(0, 150, 136))
    private fun drawCrow(canvas: Canvas, c: Float) = drawGenericBird(canvas, c, Color.rgb(38, 50, 56))

    private fun drawGenericBird(canvas: Canvas, c: Float, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color
        canvas.drawOval(RectF(c - 85f, c - 60f, c + 85f, c + 75f), p)
        canvas.drawCircle(c + 40f, c - 55f, 45f, p)
        // Wing
        p.color = Color.argb(180, Color.red(color) / 2, Color.green(color) / 2, Color.blue(color) / 2)
        canvas.drawOval(RectF(c - 75f, c - 10f, c + 15f, c + 60f), p)
        // Beak
        p.color = Color.rgb(255, 179, 0)
        val beak = Path().apply { moveTo(c + 75f, c - 65f); lineTo(c + 120f, c - 50f); lineTo(c + 75f, c - 40f); close() }
        canvas.drawPath(beak, p)

        drawEyes(canvas, c + 45f, c - 65f, c + 45f, c - 65f)
        drawCheeks(canvas, c + 35f, c - 40f, c + 35f, c - 40f)
    }

    // --- FRUITS ---
    private fun drawFruitItem(canvas: Canvas, id: String, c: Float) {
        when (id) {
            "apple" -> drawApple(canvas, c)
            "banana" -> drawBanana(canvas, c)
            "mango" -> drawMango(canvas, c)
            "orange" -> drawOrange(canvas, c)
            "grapes" -> drawGrapes(canvas, c)
            "strawberry" -> drawStrawberry(canvas, c)
            "watermelon" -> drawWatermelon(canvas, c)
            "pineapple" -> drawPineapple(canvas, c)
            "cherry" -> drawCherry(canvas, c)
            "kiwi" -> drawKiwi(canvas, c)
            "peach" -> drawPeach(canvas, c)
            "lemon" -> drawLemon(canvas, c)
            else -> drawCuteCircleFruit(canvas, c, Color.rgb(233, 30, 99), id)
        }
    }

    private fun drawApple(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Stem & Leaf
        p.color = Color.rgb(109, 76, 65)
        canvas.drawRect(c - 8f, c - 145f, c + 8f, c - 85f, p)
        p.color = Color.rgb(76, 175, 80)
        canvas.drawOval(RectF(c + 5f, c - 140f, c + 65f, c - 95f), p)

        // Apple body (two overlapping circles for dimple)
        p.color = Color.rgb(229, 57, 53)
        canvas.drawCircle(c - 45f, c, 85f, p)
        canvas.drawCircle(c + 45f, c, 85f, p)

        // Highlight
        p.color = Color.WHITE
        canvas.drawCircle(c - 55f, c - 45f, 15f, p)

        drawFaceOnFruit(canvas, c)
    }

    private fun drawBanana(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(255, 235, 59)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 75f
        p.strokeCap = Paint.Cap.ROUND
        val b = Path().apply {
            moveTo(c - 100f, c - 70f)
            quadTo(c + 20f, c + 110f, c + 110f, c - 30f)
        }
        canvas.drawPath(b, p)
        p.style = Paint.Style.FILL

        // Stem & tip
        p.color = Color.rgb(102, 187, 106)
        canvas.drawCircle(c - 100f, c - 70f, 22f, p)
        p.color = Color.rgb(121, 85, 72)
        canvas.drawCircle(c + 110f, c - 30f, 16f, p)

        drawFaceOnFruit(canvas, c)
    }

    private fun drawOrange(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(255, 152, 0)
        canvas.drawCircle(c, c, 105f, p)
        // Green leaf
        p.color = Color.rgb(76, 175, 80)
        canvas.drawOval(RectF(c - 15f, c - 135f, c + 45f, c - 95f), p)

        drawFaceOnFruit(canvas, c)
    }

    private fun drawStrawberry(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Berry heart/drop shape
        p.color = Color.rgb(229, 57, 53)
        val berry = Path().apply {
            moveTo(c, c + 115f)
            quadTo(c - 115f, c + 40f, c - 80f, c - 50f)
            quadTo(c, c - 70f, c + 80f, c - 50f)
            quadTo(c + 115f, c + 40f, c, c + 115f)
            close()
        }
        canvas.drawPath(berry, p)

        // Green leaves on top
        p.color = Color.rgb(76, 175, 80)
        for (i in -2..2) {
            canvas.drawOval(RectF(c + i * 30f - 18f, c - 95f, c + i * 30f + 18f, c - 45f), p)
        }

        // Yellow seeds
        p.color = Color.rgb(255, 235, 59)
        for (row in 0..3) {
            for (col in -2..2) {
                if (Math.abs(col) <= 3 - row) {
                    canvas.drawCircle(c + col * 28f, c - 20f + row * 28f, 5f, p)
                }
            }
        }

        drawFaceOnFruit(canvas, c)
    }

    private fun drawGrapes(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Stem
        p.color = Color.rgb(109, 76, 65)
        canvas.drawRect(c - 6f, c - 135f, c + 6f, c - 85f, p)

        // Grape cluster
        p.color = Color.rgb(156, 39, 176)
        val pattern = listOf(4, 3, 2, 1)
        for ((r, count) in pattern.withIndex()) {
            val startX = c - (count - 1) * 25f
            for (i in 0 until count) {
                canvas.drawCircle(startX + i * 50f, c - 60f + r * 45f, 26f, p)
            }
        }

        drawFaceOnFruit(canvas, c)
    }

    private fun drawWatermelon(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Green rind slice
        p.color = Color.rgb(56, 142, 60)
        canvas.drawArc(RectF(c - 130f, c - 110f, c + 130f, c + 110f), 0f, 180f, true, p)
        // White inner rind
        p.color = Color.WHITE
        canvas.drawArc(RectF(c - 120f, c - 100f, c + 120f, c + 100f), 0f, 180f, true, p)
        // Red flesh
        p.color = Color.rgb(239, 83, 80)
        canvas.drawArc(RectF(c - 110f, c - 90f, c + 110f, c + 90f), 0f, 180f, true, p)

        // Black seeds
        p.color = Color.rgb(33, 33, 33)
        canvas.drawCircle(c - 65f, c + 15f, 6f, p)
        canvas.drawCircle(c - 25f, c + 45f, 6f, p)
        canvas.drawCircle(c + 25f, c + 45f, 6f, p)
        canvas.drawCircle(c + 65f, c + 15f, 6f, p)

        drawFaceOnFruit(canvas, c)
    }

    private fun drawPineapple(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Crown leaves
        p.color = Color.rgb(67, 160, 71)
        for (i in -2..2) {
            canvas.drawOval(RectF(c + i * 20f - 15f, c - 145f, c + i * 20f + 15f, c - 55f), p)
        }

        // Body
        p.color = Color.rgb(255, 179, 0)
        canvas.drawOval(RectF(c - 80f, c - 65f, c + 80f, c + 105f), p)

        // Texture diamonds
        p.color = Color.rgb(245, 124, 0)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 5f
        canvas.drawLine(c - 60f, c - 30f, c + 60f, c + 60f, p)
        canvas.drawLine(c - 60f, c + 10f, c + 50f, c + 85f, p)
        canvas.drawLine(c + 60f, c - 30f, c - 60f, c + 60f, p)
        canvas.drawLine(c + 60f, c + 10f, c - 50f, c + 85f, p)
        p.style = Paint.Style.FILL

        drawFaceOnFruit(canvas, c)
    }

    private fun drawCherry(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Stems
        p.color = Color.rgb(102, 187, 106)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 8f
        val s1 = Path().apply { moveTo(c - 45f, c + 20f); quadTo(c - 20f, c - 95f, c, c - 120f) }
        val s2 = Path().apply { moveTo(c + 45f, c + 35f); quadTo(c + 20f, c - 95f, c, c - 120f) }
        canvas.drawPath(s1, p); canvas.drawPath(s2, p)
        p.style = Paint.Style.FILL

        // Leaf
        canvas.drawOval(RectF(c - 10f, c - 135f, c + 45f, c - 105f), p)

        // Two berries
        p.color = Color.rgb(194, 24, 91)
        canvas.drawCircle(c - 45f, c + 35f, 45f, p)
        canvas.drawCircle(c + 45f, c + 50f, 45f, p)

        drawEyes(canvas, c - 55f, c + 30f, c - 35f, c + 30f)
        drawEyes(canvas, c + 35f, c + 45f, c + 55f, c + 45f)
    }

    private fun drawMango(canvas: Canvas, c: Float) = drawCuteCircleFruit(canvas, c, Color.rgb(255, 179, 0), "mango")
    private fun drawKiwi(canvas: Canvas, c: Float) = drawCuteCircleFruit(canvas, c, Color.rgb(139, 195, 74), "kiwi")
    private fun drawPeach(canvas: Canvas, c: Float) = drawCuteCircleFruit(canvas, c, Color.rgb(255, 171, 145), "peach")
    private fun drawLemon(canvas: Canvas, c: Float) = drawCuteCircleFruit(canvas, c, Color.rgb(255, 235, 59), "lemon")

    // --- VEGETABLES ---
    private fun drawVegetableItem(canvas: Canvas, id: String, c: Float) {
        when (id) {
            "carrot" -> drawCarrot(canvas, c)
            "tomato" -> drawTomato(canvas, c)
            "potato" -> drawPotato(canvas, c)
            "peas" -> drawPeas(canvas, c)
            "corn" -> drawCorn(canvas, c)
            "broccoli" -> drawBroccoli(canvas, c)
            "pumpkin" -> drawPumpkin(canvas, c)
            "cucumber" -> drawCucumber(canvas, c)
            "radish" -> drawRadish(canvas, c)
            "onion" -> drawOnion(canvas, c)
            "cabbage" -> drawCabbage(canvas, c)
            "spinach" -> drawSpinach(canvas, c)
            else -> drawCuteCircleFruit(canvas, c, Color.rgb(76, 175, 80), id)
        }
    }

    private fun drawCarrot(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Green fronds
        p.color = Color.rgb(76, 175, 80)
        canvas.drawOval(RectF(c - 40f, c - 155f, c - 15f, c - 75f), p)
        canvas.drawOval(RectF(c - 12f, c - 165f, c + 12f, c - 75f), p)
        canvas.drawOval(RectF(c + 15f, c - 155f, c + 40f, c - 75f), p)

        // Orange cone
        p.color = Color.rgb(255, 109, 0)
        val carrot = Path().apply {
            moveTo(c - 65f, c - 70f)
            lineTo(c + 65f, c - 70f)
            lineTo(c, c + 125f)
            close()
        }
        canvas.drawPath(carrot, p)

        drawFaceOnFruit(canvas, c - 15f)
    }

    private fun drawTomato(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(229, 57, 53)
        canvas.drawCircle(c, c, 105f, p)
        // Star stem
        p.color = Color.rgb(76, 175, 80)
        for (i in 0 until 5) {
            val a = i * (Math.PI * 2 / 5)
            canvas.drawOval(RectF(c - 10f + (25f * Math.cos(a)).toFloat(), c - 105f + (25f * Math.sin(a)).toFloat(), c + 10f + (25f * Math.cos(a)).toFloat(), c - 85f + (25f * Math.sin(a)).toFloat()), p)
        }
        drawFaceOnFruit(canvas, c)
    }

    private fun drawCorn(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Green husk leaves
        p.color = Color.rgb(102, 187, 106)
        canvas.drawOval(RectF(c - 95f, c - 20f, c, c + 105f), p)
        canvas.drawOval(RectF(c, c - 20f, c + 95f, c + 105f), p)

        // Yellow ear
        p.color = Color.rgb(253, 216, 53)
        canvas.drawOval(RectF(c - 55f, c - 105f, c + 55f, c + 85f), p)

        drawFaceOnFruit(canvas, c - 10f)
    }

    private fun drawBroccoli(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Stalk
        p.color = Color.rgb(165, 214, 167)
        canvas.drawRoundRect(RectF(c - 30f, c, c + 30f, c + 115f), 15f, 15f, p)

        // Fluffy green florets
        p.color = Color.rgb(46, 125, 50)
        for (i in 0 until 8) {
            val a = i * (Math.PI * 2 / 8)
            canvas.drawCircle(c + (65f * Math.cos(a)).toFloat(), c - 40f + (65f * Math.sin(a)).toFloat(), 42f, p)
        }
        canvas.drawCircle(c, c - 40f, 65f, p)

        drawFaceOnFruit(canvas, c + 40f)
    }

    private fun drawPumpkin(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Stem
        p.color = Color.rgb(76, 175, 80)
        canvas.drawRect(c - 12f, c - 135f, c + 12f, c - 85f, p)

        // Ribs
        p.color = Color.rgb(245, 124, 0)
        canvas.drawOval(RectF(c - 115f, c - 80f, c + 115f, c + 95f), p)
        p.color = Color.rgb(255, 152, 0)
        canvas.drawOval(RectF(c - 85f, c - 85f, c + 85f, c + 95f), p)
        canvas.drawOval(RectF(c - 45f, c - 90f, c + 45f, c + 95f), p)

        drawFaceOnFruit(canvas, c)
    }

    private fun drawPotato(canvas: Canvas, c: Float) = drawCuteCircleFruit(canvas, c, Color.rgb(141, 110, 99), "potato")
    private fun drawPeas(canvas: Canvas, c: Float) = drawCuteCircleFruit(canvas, c, Color.rgb(129, 199, 132), "peas")
    private fun drawCucumber(canvas: Canvas, c: Float) = drawCuteCircleFruit(canvas, c, Color.rgb(67, 160, 71), "cucumber")
    private fun drawRadish(canvas: Canvas, c: Float) = drawCuteCircleFruit(canvas, c, Color.rgb(239, 83, 80), "radish")
    private fun drawOnion(canvas: Canvas, c: Float) = drawCuteCircleFruit(canvas, c, Color.rgb(186, 104, 200), "onion")
    private fun drawCabbage(canvas: Canvas, c: Float) = drawCuteCircleFruit(canvas, c, Color.rgb(129, 199, 132), "cabbage")
    private fun drawSpinach(canvas: Canvas, c: Float) = drawCuteCircleFruit(canvas, c, Color.rgb(46, 125, 50), "spinach")

    // --- VEHICLES ---
    private fun drawVehicleItem(canvas: Canvas, id: String, c: Float) {
        when (id) {
            "car" -> drawCar(canvas, c)
            "bus" -> drawBus(canvas, c)
            "truck" -> drawTruck(canvas, c)
            "airplane" -> drawAirplane(canvas, c)
            "train" -> drawTrain(canvas, c)
            "ship" -> drawShip(canvas, c)
            "bicycle" -> drawBicycle(canvas, c)
            "motorcycle" -> drawMotorcycle(canvas, c)
            "ambulance" -> drawAmbulance(canvas, c)
            "fire_truck" -> drawFireTruck(canvas, c)
            "tractor" -> drawTractor(canvas, c)
            "helicopter" -> drawHelicopter(canvas, c)
            else -> drawCuteCircleVehicle(canvas, c, Color.rgb(255, 152, 0), id)
        }
    }

    private fun drawCar(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Red car body
        p.color = Color.rgb(229, 57, 53)
        canvas.drawRoundRect(RectF(c - 125f, c - 20f, c + 125f, c + 60f), 25f, 25f, p)
        canvas.drawRoundRect(RectF(c - 75f, c - 85f, c + 75f, c - 15f), 30f, 30f, p)

        // Windows
        p.color = Color.rgb(179, 229, 252)
        canvas.drawRoundRect(RectF(c - 65f, c - 75f, c - 5f, c - 25f), 15f, 15f, p)
        canvas.drawRoundRect(RectF(c + 5f, c - 75f, c + 65f, c - 25f), 15f, 15f, p)

        // Wheels
        p.color = Color.rgb(33, 33, 33)
        canvas.drawCircle(c - 70f, c + 60f, 32f, p)
        canvas.drawCircle(c + 70f, c + 60f, 32f, p)
        p.color = Color.WHITE
        canvas.drawCircle(c - 70f, c + 60f, 14f, p)
        canvas.drawCircle(c + 70f, c + 60f, 14f, p)

        // Headlight
        p.color = Color.rgb(255, 235, 59)
        canvas.drawCircle(c + 120f, c + 10f, 14f, p)

        drawEyes(canvas, c - 35f, c - 50f, c + 35f, c - 50f)
    }

    private fun drawBus(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Yellow school bus body
        p.color = Color.rgb(255, 214, 0)
        canvas.drawRoundRect(RectF(c - 130f, c - 75f, c + 130f, c + 55f), 20f, 20f, p)

        // Windows
        p.color = Color.rgb(225, 245, 254)
        for (i in -2..2) {
            canvas.drawRoundRect(RectF(c + i * 45f - 18f, c - 55f, c + i * 45f + 18f, c - 15f), 8f, 8f, p)
        }

        // Wheels
        p.color = Color.rgb(33, 33, 33)
        canvas.drawCircle(c - 75f, c + 55f, 30f, p)
        canvas.drawCircle(c + 75f, c + 55f, 30f, p)
        p.color = Color.rgb(189, 189, 189)
        canvas.drawCircle(c - 75f, c + 55f, 12f, p)
        canvas.drawCircle(c + 75f, c + 55f, 12f, p)

        drawEyes(canvas, c - 20f, c - 35f, c + 20f, c - 35f)
    }

    private fun drawTrain(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Train engine body
        p.color = Color.rgb(30, 136, 229)
        canvas.drawRect(c - 110f, c - 35f, c + 75f, c + 55f, p)
        // Cabin
        canvas.drawRect(c + 20f, c - 95f, c + 105f, c + 55f, p)
        // Chimney
        p.color = Color.rgb(229, 57, 53)
        canvas.drawRect(c - 85f, c - 85f, c - 55f, c - 35f, p)
        // Smoke puffs
        p.color = Color.rgb(224, 224, 224)
        canvas.drawCircle(c - 70f, c - 115f, 18f, p)
        canvas.drawCircle(c - 50f, c - 135f, 24f, p)

        // Wheels
        p.color = Color.rgb(33, 33, 33)
        canvas.drawCircle(c - 70f, c + 60f, 26f, p)
        canvas.drawCircle(c - 15f, c + 60f, 26f, p)
        canvas.drawCircle(c + 60f, c + 60f, 35f, p)

        drawEyes(canvas, c + 50f, c - 60f, c + 75f, c - 60f)
    }

    private fun drawAirplane(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(66, 165, 245)
        // Fuselage
        canvas.drawRoundRect(RectF(c - 130f, c - 30f, c + 120f, c + 30f), 30f, 30f, p)
        // Wings
        val wing = Path().apply {
            moveTo(c - 20f, c); lineTo(c - 60f, c - 115f); lineTo(c + 20f, c); lineTo(c - 60f, c + 115f); close()
        }
        canvas.drawPath(wing, p)

        // Tail fin
        p.color = Color.rgb(239, 83, 80)
        val tail = Path().apply {
            moveTo(c - 125f, c); lineTo(c - 145f, c - 65f); lineTo(c - 95f, c); close()
        }
        canvas.drawPath(tail, p)

        // Windows
        p.color = Color.WHITE
        canvas.drawCircle(c + 25f, c, 12f, p)
        canvas.drawCircle(c + 65f, c, 12f, p)

        drawEyes(canvas, c + 75f, c - 10f, c + 75f, c - 10f)
    }

    private fun drawShip(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Hull
        p.color = Color.rgb(229, 57, 53)
        val hull = Path().apply {
            moveTo(c - 125f, c + 10f)
            lineTo(c + 125f, c + 10f)
            lineTo(c + 90f, c + 75f)
            lineTo(c - 90f, c + 75f)
            close()
        }
        canvas.drawPath(hull, p)

        // Deck cabin
        p.color = Color.WHITE
        canvas.drawRect(c - 65f, c - 45f, c + 65f, c + 10f, p)
        // Chimney
        p.color = Color.rgb(255, 179, 0)
        canvas.drawRect(c - 20f, c - 85f, c + 20f, c - 45f, p)

        // Waves
        p.color = Color.rgb(41, 182, 246)
        for (i in -3..3) {
            canvas.drawCircle(c + i * 40f, c + 80f, 25f, p)
        }

        drawEyes(canvas, c - 20f, c - 20f, c + 20f, c - 20f)
    }

    private fun drawFireTruck(canvas: Canvas, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        // Red body
        p.color = Color.rgb(211, 47, 47)
        canvas.drawRoundRect(RectF(c - 125f, c - 45f, c + 125f, c + 55f), 15f, 15f, p)
        // Ladder
        p.color = Color.rgb(189, 189, 189)
        canvas.drawRect(c - 105f, c - 75f, c + 35f, c - 55f, p)
        // Flashing siren
        p.color = Color.rgb(33, 150, 243)
        canvas.drawCircle(c + 85f, c - 60f, 16f, p)

        // Wheels
        p.color = Color.rgb(33, 33, 33)
        canvas.drawCircle(c - 70f, c + 60f, 30f, p)
        canvas.drawCircle(c + 70f, c + 60f, 30f, p)

        drawEyes(canvas, c + 50f, c - 15f, c + 85f, c - 15f)
    }

    private fun drawTruck(canvas: Canvas, c: Float) = drawCuteCircleVehicle(canvas, c, Color.rgb(30, 136, 229), "truck")
    private fun drawBicycle(canvas: Canvas, c: Float) = drawCuteCircleVehicle(canvas, c, Color.rgb(76, 175, 80), "bicycle")
    private fun drawMotorcycle(canvas: Canvas, c: Float) = drawCuteCircleVehicle(canvas, c, Color.rgb(255, 112, 67), "motorcycle")
    private fun drawAmbulance(canvas: Canvas, c: Float) = drawCuteCircleVehicle(canvas, c, Color.WHITE, "ambulance")
    private fun drawTractor(canvas: Canvas, c: Float) = drawCuteCircleVehicle(canvas, c, Color.rgb(104, 159, 56), "tractor")
    private fun drawHelicopter(canvas: Canvas, c: Float) = drawCuteCircleVehicle(canvas, c, Color.rgb(255, 167, 38), "helicopter")

    // --- SHAPES & COLORS ---
    private fun drawShapeColorItem(canvas: Canvas, id: String, c: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        when (id) {
            "red_circle" -> {
                p.color = Color.rgb(229, 57, 53)
                canvas.drawCircle(c, c, 115f, p)
                drawFaceOnShape(canvas, c)
            }
            "blue_square" -> {
                p.color = Color.rgb(33, 150, 243)
                canvas.drawRoundRect(RectF(c - 105f, c - 105f, c + 105f, c + 105f), 25f, 25f, p)
                drawFaceOnShape(canvas, c)
            }
            "yellow_triangle" -> {
                p.color = Color.rgb(255, 214, 0)
                val tri = Path().apply {
                    moveTo(c, c - 120f); lineTo(c + 120f, c + 95f); lineTo(c - 120f, c + 95f); close()
                }
                canvas.drawPath(tri, p)
                drawFaceOnShape(canvas, c + 10f)
            }
            "green_star" -> {
                p.color = Color.rgb(76, 175, 80)
                drawStarShape(canvas, c, 130f, 60f, p)
                drawFaceOnShape(canvas, c)
            }
            "purple_heart" -> {
                p.color = Color.rgb(156, 39, 176)
                drawHeartShape(canvas, c, p)
                drawFaceOnShape(canvas, c)
            }
            "orange_diamond" -> {
                p.color = Color.rgb(255, 152, 0)
                val dia = Path().apply {
                    moveTo(c, c - 120f); lineTo(c + 110f, c); lineTo(c, c + 120f); lineTo(c - 110f, c); close()
                }
                canvas.drawPath(dia, p)
                drawFaceOnShape(canvas, c)
            }
            "pink_oval" -> {
                p.color = Color.rgb(240, 98, 146)
                canvas.drawOval(RectF(c - 125f, c - 85f, c + 125f, c + 85f), p)
                drawFaceOnShape(canvas, c)
            }
            "brown_rectangle" -> {
                p.color = Color.rgb(121, 85, 72)
                canvas.drawRoundRect(RectF(c - 130f, c - 75f, c + 130f, c + 75f), 20f, 20f, p)
                drawFaceOnShape(canvas, c)
            }
            "black_crescent" -> {
                p.color = Color.rgb(38, 50, 56)
                val moon = Path().apply {
                    addCircle(c - 15f, c, 110f, Path.Direction.CW)
                }
                val cut = Path().apply {
                    addCircle(c + 35f, c - 30f, 95f, Path.Direction.CW)
                }
                moon.op(cut, Path.Op.DIFFERENCE)
                canvas.drawPath(moon, p)
                drawFaceOnShape(canvas, c - 35f)
            }
            "white_cloud" -> {
                p.color = Color.rgb(225, 245, 254)
                canvas.drawCircle(c - 60f, c + 15f, 48f, p)
                canvas.drawCircle(c + 60f, c + 15f, 48f, p)
                canvas.drawCircle(c - 20f, c - 30f, 65f, p)
                canvas.drawCircle(c + 30f, c - 20f, 55f, p)
                canvas.drawRoundRect(RectF(c - 70f, c + 5f, c + 70f, c + 65f), 20f, 20f, p)
                drawFaceOnShape(canvas, c)
            }
            "rainbow" -> {
                val bands = listOf(
                    Color.rgb(229, 57, 53),
                    Color.rgb(255, 152, 0),
                    Color.rgb(255, 235, 59),
                    Color.rgb(76, 175, 80),
                    Color.rgb(33, 150, 243),
                    Color.rgb(156, 39, 176)
                )
                p.style = Paint.Style.STROKE
                p.strokeWidth = 16f
                for ((i, col) in bands.withIndex()) {
                    p.color = col
                    val r = 145f - i * 16f
                    canvas.drawArc(RectF(c - r, c - r + 30f, c + r, c + r + 30f), 180f, 180f, false, p)
                }
                p.style = Paint.Style.FILL
            }
            "gray_hexagon" -> {
                p.color = Color.rgb(120, 144, 156)
                val hex = Path()
                for (i in 0 until 6) {
                    val a = i * Math.PI / 3
                    val x = c + (115f * Math.cos(a)).toFloat()
                    val y = c + (115f * Math.sin(a)).toFloat()
                    if (i == 0) hex.moveTo(x, y) else hex.lineTo(x, y)
                }
                hex.close()
                canvas.drawPath(hex, p)
                drawFaceOnShape(canvas, c)
            }
            else -> drawCuteCircleFruit(canvas, c, Color.rgb(0, 150, 136), id)
        }
    }

    private fun drawStarShape(canvas: Canvas, c: Float, outer: Float, inner: Float, paint: Paint) {
        val path = Path()
        for (i in 0 until 10) {
            val r = if (i % 2 == 0) outer else inner
            val a = i * Math.PI / 5 - Math.PI / 2
            val x = c + (r * Math.cos(a)).toFloat()
            val y = c + (r * Math.sin(a)).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    private fun drawHeartShape(canvas: Canvas, c: Float, paint: Paint) {
        val path = Path().apply {
            moveTo(c, c + 105f)
            cubicTo(c - 120f, c + 20f, c - 120f, c - 85f, c - 50f, c - 85f)
            cubicTo(c - 15f, c - 85f, c, c - 55f, c, c - 35f)
            cubicTo(c, c - 55f, c + 15f, c - 85f, c + 50f, c - 85f)
            cubicTo(c + 120f, c - 85f, c + 120f, c + 20f, c, c + 105f)
            close()
        }
        canvas.drawPath(path, paint)
    }

    // --- REUSABLE FACIAL EXPRESSIONS ---
    private fun drawEyes(canvas: Canvas, lx: Float, ly: Float, rx: Float, ry: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(33, 33, 33)
        canvas.drawCircle(lx, ly, 14f, p)
        canvas.drawCircle(rx, ry, 14f, p)
        // White sparkle glints
        p.color = Color.WHITE
        canvas.drawCircle(lx - 4f, ly - 4f, 5f, p)
        canvas.drawCircle(rx - 4f, ry - 4f, 5f, p)
    }

    private fun drawSparkleEyes(canvas: Canvas, lx: Float, ly: Float, rx: Float, ry: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.rgb(33, 33, 33)
        canvas.drawCircle(lx, ly, 18f, p)
        canvas.drawCircle(rx, ry, 18f, p)
        p.color = Color.WHITE
        canvas.drawCircle(lx - 5f, ly - 5f, 7f, p)
        canvas.drawCircle(lx + 4f, ly + 4f, 3f, p)
        canvas.drawCircle(rx - 5f, ry - 5f, 7f, p)
        canvas.drawCircle(rx + 4f, ry + 4f, 3f, p)
    }

    private fun drawCheeks(canvas: Canvas, lx: Float, ly: Float, rx: Float, ry: Float) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = Color.argb(120, 255, 128, 171)
        canvas.drawCircle(lx, ly, 16f, p)
        canvas.drawCircle(rx, ry, 16f, p)
    }

    private fun drawFaceOnFruit(canvas: Canvas, c: Float) {
        drawEyes(canvas, c - 32f, c - 10f, c + 32f, c - 10f)
        drawCheeks(canvas, c - 50f, c + 15f, c + 50f, c + 15f)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 5f
        p.color = Color.rgb(33, 33, 33)
        canvas.drawArc(RectF(c - 20f, c, c + 20f, c + 25f), 10f, 160f, false, p)
    }

    private fun drawFaceOnShape(canvas: Canvas, c: Float) {
        drawSparkleEyes(canvas, c - 35f, c - 15f, c + 35f, c - 15f)
        drawCheeks(canvas, c - 55f, c + 15f, c + 55f, c + 15f)
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.style = Paint.Style.STROKE
        p.strokeWidth = 6f
        p.color = Color.rgb(33, 33, 33)
        canvas.drawArc(RectF(c - 25f, c, c + 25f, c + 30f), 10f, 160f, false, p)
    }

    private fun drawCuteCircleAnimal(canvas: Canvas, c: Float, color: Int, name: String) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color
        canvas.drawCircle(c, c, 105f, p)
        drawFaceOnFruit(canvas, c)
    }

    private fun drawCuteCircleFruit(canvas: Canvas, c: Float, color: Int, name: String) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color
        canvas.drawCircle(c, c, 105f, p)
        // Green stem/leaf
        p.color = Color.rgb(76, 175, 80)
        canvas.drawOval(RectF(c - 10f, c - 130f, c + 35f, c - 95f), p)
        drawFaceOnFruit(canvas, c)
    }

    private fun drawCuteCircleVehicle(canvas: Canvas, c: Float, color: Int, name: String) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color
        canvas.drawRoundRect(RectF(c - 110f, c - 55f, c + 110f, c + 45f), 20f, 20f, p)
        // Wheels
        p.color = Color.rgb(33, 33, 33)
        canvas.drawCircle(c - 65f, c + 50f, 28f, p)
        canvas.drawCircle(c + 65f, c + 50f, 28f, p)
        drawEyes(canvas, c - 25f, c - 15f, c + 25f, c - 15f)
    }

    private fun drawGenericItem(canvas: Canvas, id: String, c: Float, color: Int) {
        val p = Paint(Paint.ANTI_ALIAS_FLAG)
        p.color = color
        canvas.drawCircle(c, c, 105f, p)
        drawEyes(canvas, c - 35f, c - 20f, c + 35f, c - 20f)
        drawCheeks(canvas, c - 55f, c + 15f, c + 55f, c + 15f)
    }
}
