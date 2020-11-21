package com.wgw.photo.preview.util;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

/**
 * 构建富文本
 *
 * @author Created by wanggaowan on 2020/10/26 08:32
 */
public class SpannableString extends android.text.SpannableString {
    
    public SpannableString(CharSequence source) {
        super(source);
    }
    
    public static class AppendBuilder {
        
        private List<AppendSpan> mSpans;
        
        public AppendBuilder() {
            mSpans = new ArrayList<>();
        }
        
        /**
         * 添加一个Span
         *
         * @param spanStr 需要处理的文本，根据addSpan顺序拼接到整个文本内容中
         * @return 如果数据无效，则返回一个无效的Span节点，仅仅为了流式调用，实际执行时跳过
         */
        public AppendSpan addSpan(String spanStr) {
            AppendSpan span;
            if (!TextUtils.isEmpty(spanStr)) {
                span = new AppendSpan(this, spanStr);
                mSpans.add(span);
            } else {
                span = new AppendSpan(this, null);
            }
            return span;
        }
        
        /**
         * 应用SpannableString至目标
         *
         * @param textView 展示内容的TextView或子对象，如果设置的span中包含点击项，
         *                 则将调用{@link TextView#setMovementMethod(MovementMethod)},参数为LinkMovementMethod实例
         */
        public <T extends TextView> void apply(T textView) {
            if (textView == null) {
                return;
            }
            
            if (mSpans.size() == 0) {
                textView.setText(null);
                return;
            }
            
            StringBuilder builder = new StringBuilder();
            for (AppendSpan span : mSpans) {
                builder.append(span.spanStr);
            }
            
            SpannableString ss = new SpannableString(builder.toString());
            boolean needClick = false;
            int start = 0;
            for (AppendSpan span : mSpans) {
                
                if (span.couldClick) {
                    needClick = true;
                }
                
                int end = start + span.spanStr.length();
                ss.setSpan(new SpanImpl(span), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                start = end;
            }
            
            if (needClick) {
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
            textView.setText(ss);
            
            mSpans.clear();
            mSpans = null;
        }
        
        /**
         * 构建SpannableString，如果构建对象中有可点击Span，请确保应用该文本的{@link TextView}或子类设置了可点击的MovementMethod。
         * 否则推荐使用{@link #apply(TextView)}方法
         */
        public SpannableString create() {
            if (mSpans.size() == 0) {
                return new SpannableString("");
            }
            
            StringBuilder builder = new StringBuilder();
            for (AppendSpan span : mSpans) {
                builder.append(span.spanStr);
            }
            
            SpannableString ss = new SpannableString(builder.toString());
            int start = 0;
            for (AppendSpan span : mSpans) {
                int end = start + span.spanStr.length();
                ss.setSpan(new SpanImpl(span), start, end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                start = end;
            }
            
            mSpans.clear();
            mSpans = null;
            return ss;
        }
    }
    
    /**
     * 追加模式，所有Span以拼接方式最终展示到目标View上
     */
    public static class AppendSpan {
        public static final int INVALID_VALUE = -1;
        
        protected final AppendBuilder builder;
        
        protected String spanStr;
        protected int size = INVALID_VALUE;
        protected int color = INVALID_VALUE;
        protected boolean couldClick;
        protected OnSpanClickListener clickListener;
        protected boolean underLine;
        protected Typeface typeface;
        
        AppendSpan(AppendBuilder builder, String spanStr) {
            this.builder = builder;
            this.spanStr = spanStr;
        }
        
        /**
         * 文本字体大小,不指定不调用此方法或传{@link AppendSpan#INVALID_VALUE}
         */
        public AppendSpan size(int size) {
            this.size = size;
            return this;
        }
        
        public AppendSpan typeface(Typeface typeface) {
            this.typeface = typeface;
            return this;
        }
        
        /**
         * 文本字体颜色，不指定不调用此方法或传{@link AppendSpan#INVALID_VALUE}
         */
        public AppendSpan color(@ColorInt int color) {
            this.color = color;
            return this;
        }
        
        /**
         * 文本是否可点击
         */
        public AppendSpan couldClick(boolean cloudClick) {
            this.couldClick = cloudClick;
            return this;
        }
        
        /**
         * 文本是否需要展示下划线
         */
        public AppendSpan underLine(boolean underLine) {
            this.underLine = underLine;
            return this;
        }
        
        /**
         * 文本点检监听，需要{@link #couldClick(boolean)}设置为true
         */
        public AppendSpan clickListener(OnSpanClickListener listener) {
            clickListener = listener;
            return this;
        }
        
        /**
         * 添加一个Span
         *
         * @param spanStr 需要处理的文本，根据addSpan顺序拼接到整个文本内容中
         * @return 如果数据无效，则返回一个无效的Span节点，仅仅为了流式调用，实际执行时跳过
         */
        public AppendSpan addSpan(String spanStr) {
            return builder.addSpan(spanStr);
        }
        
        /**
         * 应用SpannableString至目标
         *
         * @param textView 展示内容的TextView或子对象，如果设置的span中包含点击项，
         *                 则将调用{@link TextView#setMovementMethod(MovementMethod)},参数为LinkMovementMethod实例
         */
        public <T extends TextView> void apply(@NonNull T textView) {
            builder.apply(textView);
        }
        
        /**
         * 构建SpannableString，如果构建对象中有可点击Span，请确保应用该文本的{@link TextView}或子类设置了可点击的MovementMethod。
         * 否则推荐使用{@link #apply(TextView)}方法
         */
        public SpannableString create() {
            return builder.create();
        }
    }
    
    /**
     * SpannableString构建器
     */
    public static class Builder extends AppendBuilder {
        
        private String source;
        private List<Span> mSpans;
        
        private Builder() {
        
        }
        
        /**
         * @param source 构建的SpannableString需要展示的原始文本，还未进行处理过.如果为空串""，则不处理
         */
        public Builder(@NonNull String source, Object... args) {
            mSpans = new ArrayList<>();
            this.source = source;
            if (isSourceValid() && args != null && args.length > 0) {
                this.source = String.format(this.source, args);
            }
        }
        
        private boolean isSourceValid() {
            return !TextUtils.isEmpty(source);
        }
        
        /**
         * 以追加模式进行构建
         */
        public static AppendBuilder appendMode() {
            return new AppendBuilder();
        }
        
        /**
         * @param source 构建的SpannableString需要展示的原始文本，还未进行处理过.如果为空串""，则不处理
         */
        public static Builder string(@NonNull String source) {
            return new Builder(source);
        }
        
        /**
         * @param source 构建的SpannableString需要展示的原始文本，还未进行处理过.如果为空串""，则不处理
         * @param args   source格式化参数
         */
        public static Builder string(@NonNull String source, Object... args) {
            return new Builder(source, args);
        }
        
        /**
         * 添加一个Span，描述{@link #string(String)} 或 {@link #Builder(String, Object...)}参数source中某一段文本应该怎么显示
         *
         * @param start 文本在source中的开始位置，如果 < 0 || > （end || source.length()) 则不处理
         * @param end   文本在source中的结束位置，如果 < 0 || > (start || source.length()) 则不处理
         * @return 如果数据无效，则返回一个无效的Span节点，仅仅为了流式调用，实际执行时跳过
         */
        public Span addSpan(int start, int end) {
            Span span;
            if (isSourceValid() && start >= 0 && start <= end && start <= source.length()) {
                String spanStr = source.substring(start, end);
                span = new Span(this, spanStr, start, end);
                mSpans.add(span);
            } else {
                span = new Span(this, null, Span.INVALID_VALUE, Span.INVALID_VALUE);
            }
            return span;
        }
        
        /**
         * 添加一个Span，描述{@link #string(String)} 或 {@link #Builder(String, Object...)}参数source中某一段文本应该怎么显示
         *
         * @param spanStr source文本中指定需要处理的片段文本内容，如果source中不存在spanStr，则不处理。
         *                请确保source不会多次出现spanStr文本，否则请使用{@link #addSpan(int, int)}明确指明区间
         * @return 如果数据无效，则返回一个无效的Span节点，仅仅为了流式调用，实际执行时跳过
         */
        @Override
        public Span addSpan(String spanStr) {
            Span span;
            if (isSourceValid() && !TextUtils.isEmpty(spanStr) && source.contains(spanStr)) {
                int index = source.indexOf(spanStr);
                span = new Span(this, spanStr, index, index + spanStr.length());
                span.spanStr = spanStr;
                mSpans.add(span);
            } else {
                span = new Span(this, null, Span.INVALID_VALUE, Span.INVALID_VALUE);
            }
            
            return span;
        }
        
        /**
         * 应用SpannableString至目标
         *
         * @param textView 展示内容的TextView或子对象，如果设置的span中包含点击项，
         *                 则将调用{@link TextView#setMovementMethod(MovementMethod)},参数为LinkMovementMethod实例
         */
        public <T extends TextView> void apply(T textView) {
            if (textView == null) {
                return;
            }
            
            if (!isSourceValid() || mSpans.size() == 0) {
                textView.setText(null);
                return;
            }
            
            SpannableString ss = new SpannableString(source);
            boolean needClick = false;
            for (Span span : mSpans) {
                
                if (span.couldClick) {
                    needClick = true;
                }
                
                ss.setSpan(new SpanImpl(span), span.start, span.end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            
            if (needClick) {
                textView.setMovementMethod(LinkMovementMethod.getInstance());
            }
            textView.setText(ss);
            
            mSpans.clear();
            mSpans = null;
            source = null;
        }
        
        /**
         * 构建SpannableString，如果构建对象中有可点击Span，请确保应用该文本的{@link TextView}或子类设置了可点击的MovementMethod。
         * 否则推荐使用{@link #apply(TextView)}方法
         */
        public SpannableString create() {
            if (!isSourceValid()) {
                return new SpannableString("");
            }
            
            SpannableString ss = new SpannableString(source);
            for (Span span : mSpans) {
                ss.setSpan(new SpanImpl(span), span.start, span.end, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
            }
            
            mSpans.clear();
            mSpans = null;
            source = null;
            return ss;
        }
    }
    
    public static class Span extends AppendSpan {
        final Builder builder;
        
        final int start;
        final int end;
        
        Span(Builder builder, String spanStr, int start, int end) {
            super(builder, spanStr);
            this.builder = builder;
            this.start = start;
            this.end = end;
        }
        
        /**
         * 添加一个Span，描述{@link Builder#string(String)} 或 {@link Builder#Builder(String, Object...)}参数source中某一段文本应该怎么显示
         *
         * @param start 文本在source中的开始位置，如果 < 0 || > （end || source.length()) 则不处理
         * @param end   文本在source中的结束位置，如果 < 0 || > (start || source.length()) 则不处理
         * @return 如果数据无效，则返回一个无效的Span节点，仅仅为了流式调用，实际执行时跳过
         */
        public Span addSpan(int start, int end) {
            return builder.addSpan(start, end);
        }
        
        /**
         * 添加一个Span，描述{@link Builder#string(String)} 或 {@link Builder#Builder(String, Object...)}参数source中某一段文本应该怎么显示
         *
         * @param spanStr source文本中指定需要处理的片段文本内容，如果source中不存在spanStr，则不处理.
         *                请确保source不会多次出现spanStr文本，否则请使用{@link #addSpan(int, int)}明确指明区间
         * @return 如果数据无效，则返回一个无效的Span节点，仅仅为了流式调用，实际执行时跳过
         */
        @Override
        public Span addSpan(String spanStr) {
            return builder.addSpan(spanStr);
        }
    }
    
    /**
     * 用于实现{@link AppendSpan}中设置的内容
     */
    static class SpanImpl extends ClickableSpan {
        
        private final AppendSpan span;
        
        SpanImpl(AppendSpan span) {
            this.span = span;
        }
        
        @Override
        public void onClick(@NonNull View widget) {
            if (span.couldClick && span.clickListener != null) {
                span.clickListener.onClick(widget, span.spanStr);
            }
        }
        
        @Override
        public void updateDrawState(@NonNull TextPaint ds) {
            if (span.size != AppendSpan.INVALID_VALUE) {
                ds.setTextSize(span.size);
            }
            
            if (span.typeface != null) {
                ds.setTypeface(span.typeface);
            }
            
            if (span.color != AppendSpan.INVALID_VALUE) {
                ds.setColor(span.color);
            }
            
            ds.setUnderlineText(span.underLine);
        }
    }
    
    /**
     * Span点击监听
     */
    public interface OnSpanClickListener {
        /**
         * @param view    被点击文本当前应用的View对象
         * @param spanStr 点检区域文本
         */
        void onClick(@NonNull View view, @NonNull String spanStr);
    }
}
