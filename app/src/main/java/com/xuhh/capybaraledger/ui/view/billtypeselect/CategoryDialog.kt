package com.xuhh.capybaraledger.ui.view.billtypeselect

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.xuhh.capybaraledger.R
import com.xuhh.capybaraledger.data.model.Category
import com.xuhh.capybaraledger.ui.view.unicode.UnicodeTextView

class CategoryDialog(
    context: Context,
    private val categories: List<Category>,
    private val selectedCategory: Category?,
    private val onCategorySelected: (Category) -> Unit
) : BottomSheetDialog(context) {

    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_category_selector)

        // 设置对话框宽度为屏幕宽度
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

        // 设置对话框位置在底部
        window?.setGravity(android.view.Gravity.BOTTOM)

        // 设置背景透明
        window?.setBackgroundDrawableResource(android.R.color.transparent)

        setupCloseButton()
        setupGridLayout()
    }

    private fun setupGridLayout() {
        val gridLayout = findViewById<GridLayout>(R.id.gr_types)
        val context = context ?: return

        gridLayout?.apply {
            removeAllViews()
            columnCount = 4
        }

        val screenWidth = context.resources.displayMetrics.widthPixels
        val horizontalPadding = 16.dpToPx() // 根据实际布局调整
        val availableWidth = screenWidth - horizontalPadding * 2

        // 计算单个分类项的宽度（考虑间距）
        val margin = 0
        val itemWidth = (availableWidth - margin * 3) / 4 // 4列有3个间隙

        // 动态添加分类项
        categories.forEachIndexed { index, category ->
            val itemView = LayoutInflater.from(context)
                .inflate(R.layout.item_bill_type_selector, gridLayout, false)

            // 使用UnicodeTextView显示图标
            itemView.findViewById<UnicodeTextView>(R.id.tv_icon)?.apply {
                text = context.getString(category.iconResId)
            }
            itemView.findViewById<TextView>(R.id.tv_name).text = category.name

            // 动态设置列位置
            val column = index % 4
            val row = index / 4

            GridLayout.LayoutParams().apply {
                width = itemWidth
                height = GridLayout.LayoutParams.WRAP_CONTENT
                columnSpec = GridLayout.spec(column)
                rowSpec = GridLayout.spec(row)
                setMargins(margin, margin, margin, margin)
                setGravity(Gravity.CENTER)
            }.also { params ->
                gridLayout?.addView(itemView, params)
            }

            // 点击事件...
            itemView.setOnClickListener {
                onCategorySelected(category)
                dismiss()
            }
        }
    }
    //关闭按钮
    private fun setupCloseButton() {
        findViewById<ImageView>(R.id.btn_close)?.setOnClickListener {
            dismiss()
        }
    }
    //dp转px
    private fun Int.dpToPx(): Int {
        return (this * context.resources.displayMetrics.density).toInt()
    }
}