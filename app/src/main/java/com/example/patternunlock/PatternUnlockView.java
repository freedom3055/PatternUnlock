package com.example.patternunlock;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PatternUnlockView extends View {

    private Cell[][] cells;
    private List<Cell> patternCells = new ArrayList<Cell>();

    private OnPatternListener patternListener;
    private DisplayMode displayMode = DisplayMode.Correct;

    private static final int BIG_CRICLE_RADIUS = 100;
    private static final int SMALLER_CRICLE_RADIUS = 30;
    private static final int ANGLE_LENGTH = BIG_CRICLE_RADIUS / 4;
    private static final int ANGLE_MARGIN = BIG_CRICLE_RADIUS / 5;
    private static final float PATH_STROKE_WIDTH = 4.0f;
    private static final int CORRECT_COLOR = 0xFF1F8EE9;
    private static final int WRONG_COLOR = 0xFFF4333C;

    private Paint paint = new Paint();
    private Paint pathPaint = new Paint();
    private Path path = new Path();
    private Path anglePath = new Path();

    public PatternUnlockView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public PatternUnlockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PatternUnlockView(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredWidth = getMeasuredWidth();
        setMeasuredDimension(measuredWidth, measuredWidth);

        initCells(measuredWidth);
    }

    public void setOnPatternListener(OnPatternListener patternListener) {
        this.patternListener = patternListener;
    }

    public void setDisplayMode(DisplayMode displayMode) {
        this.displayMode = displayMode;
    }

    private void initCells(int measuredWidth) {
        if (cells != null) {
            return;
        }
        cells = new Cell[3][3];

        float margin = (measuredWidth - BIG_CRICLE_RADIUS * 6) / 4.0f;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Cell cell = new Cell();
                cell.row = i;
                cell.column = j;
                cell.isFound = false;
                cell.x = margin * (i + 1) + 2 * BIG_CRICLE_RADIUS * i
                        + BIG_CRICLE_RADIUS;
                cell.y = margin * (j + 1) + 2 * BIG_CRICLE_RADIUS * j
                        + BIG_CRICLE_RADIUS;
                cells[i][j] = cell;
            }
        }
    }

    private void init(Context context) {
        paint.setAntiAlias(true);
        paint.setDither(true);

        pathPaint.setAntiAlias(true);
        pathPaint.setDither(true);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setStrokeCap(Paint.Cap.ROUND);
        pathPaint.setStrokeWidth(PATH_STROKE_WIDTH);
    }

    public void resetPattern() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                cells[i][j].isFound = false;
            }
        }
        patternCells.clear();
        path.rewind();

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        if (cells == null) {
            return;
        }
        pathPaint.setColor(getPathColor());
        canvas.drawPath(path, pathPaint);
        drawCircle(canvas);
        drawAngles(canvas);
    }


    private int getPathColor() {
        if (displayMode == DisplayMode.Correct) {
            return CORRECT_COLOR;
        }
        return WRONG_COLOR;
    }

    private int getCircleColor(Cell cell) {
        if (displayMode == DisplayMode.Correct || !cell.isFound) {
            return CORRECT_COLOR;
        } else {
            return WRONG_COLOR;
        }
    }

    private void drawCircle(Canvas canvas) {

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Cell cell = cells[i][j];

                if (cell.isFound) {
                    paint.setColor(Color.WHITE);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawCircle(cell.x, cell.y,
                            BIG_CRICLE_RADIUS, paint);

                    paint.setColor(getCircleColor(cell));
                    canvas.drawCircle(cell.x, cell.y,
                            SMALLER_CRICLE_RADIUS, paint);
                }

                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(PATH_STROKE_WIDTH);
                paint.setColor(getCircleColor(cell));
                canvas.drawCircle(cell.x, cell.y,
                        BIG_CRICLE_RADIUS, paint);

            }
        }
    }

    private void drawAngles(Canvas canvas) {
        if (displayMode != DisplayMode.Wrong) {
            return;
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(WRONG_COLOR);

        for (int i = 0; i < patternCells.size() - 1; i++) {
            Cell cell = patternCells.get(i);

            canvas.save();

            anglePath.rewind();
            anglePath.moveTo(cell.x + BIG_CRICLE_RADIUS - ANGLE_MARGIN, cell.y);
            anglePath.lineTo(cell.x + BIG_CRICLE_RADIUS - ANGLE_LENGTH - ANGLE_MARGIN, cell.y - ANGLE_LENGTH);
            anglePath.lineTo(cell.x + BIG_CRICLE_RADIUS - ANGLE_LENGTH - ANGLE_MARGIN, cell.y + ANGLE_LENGTH);
            anglePath.close();

            canvas.rotate(getRotateAngleDegrees(cell, patternCells.get(i + 1)), cell.x, cell.y);
            canvas.drawPath(anglePath, paint);
            canvas.restore();
        }
    }

    private float getRotateAngleDegrees(Cell firstCell, Cell nextCell) {
        float dx = nextCell.x - firstCell.x;
        float dy = nextCell.y - firstCell.y;
        if (dy == 0 && dx > 0) {
            return 0.0f;
        } else if (dy == 0 && dx < 0) {
            return 180.0f;
        } else if (dx == 0 && dy > 0) {
            return 90.0f;
        } else if (dx == 0 && dy < 0) {
            return 270.0f;
        } else if (dx > 0 && dy > 0) {
            return (float) (Math.atan(dy / dx) * 180 / Math.PI);
        } else if (dx > 0 && dy < 0) {
            return -(float) (Math.atan(-dy / dx) * 180 / Math.PI);
        } else if (dx < 0 && dy < 0) {
            return 180 + (float) (Math.atan(dy / dx) * 180 / Math.PI);
        } else if (dx < 0 && dy > 0) {
            return 180 - (float) (Math.atan(-dy / dx) * 180 / Math.PI);
        }
        return 0.0f;

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (displayMode == DisplayMode.Wrong) {
                    resetPattern();
                    return true;
                }
                float x = event.getX();
                float y = event.getY();
                Cell cell = checkForHitCell(x, y);
                if (cell != null) {
                    patternCells.add(cell);
                }
                if (!patternCells.isEmpty()) {
                    resetPath();
                    path.lineTo(x, y);
                    invalidate();
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (!patternCells.isEmpty()) {
                    displayMode = DisplayMode.Wrong;
                    resetPath();
                    invalidate();
                    handlePattern();
                } else {
                    displayMode = DisplayMode.Correct;
                }
                break;
        }
        return true;
    }

    private void handlePattern() {
        if (patternListener != null && !patternCells.isEmpty()) {
            StringBuffer stringBuffer = new StringBuffer();
            for (int i = 0; i < patternCells.size(); i++) {
                Cell cell = patternCells.get(i);
                stringBuffer.append(String.valueOf(cell.row * 3 + cell.column));
            }
            patternListener.onDrawPatternFinished(stringBuffer.toString());
        }

    }

    private void resetPath() {
        path.rewind();
        int size = patternCells.size();
        for (int i = 0; i < size; i++) {
            Cell cell2 = patternCells.get(i);
            if (i == 0) {
                path.moveTo(cell2.x, cell2.y);
            } else {
                path.lineTo(cell2.x, cell2.y);
            }
        }
    }

    private Cell checkForHitCell(float x, float y) {

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                Cell cell = cells[i][j];
                if (cell.isFound) {
                    continue;
                }
                float dx = x - cell.x;
                float dy = y - cell.y;
                float distance = (float) Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
                if (distance <= BIG_CRICLE_RADIUS) {
                    cell.isFound = true;
                    return cell;
                }
            }
        }
        return null;
    }

    private static class Cell {
        public boolean isFound;
        public int row;
        public int column;
        public float x;
        public float y;
    }

    public enum DisplayMode {
        Correct, Wrong
    }

    public interface OnPatternListener {
        void onDrawPatternFinished(String patternString);
    }

}
