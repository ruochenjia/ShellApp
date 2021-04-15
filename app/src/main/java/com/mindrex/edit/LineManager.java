package com.mindrex.edit;

import android.text.Editable;

import java.util.ArrayList;
import java.util.List;

public final class LineManager implements TextWatcher, ActionEndListener {
    private int lineCount = 1;
    private final Editable serveTarget;
    private final List<Pair> pairs;
    private final List<Pair> garbages;
    private Pair endPoint;
    private final Pair zeroPoint;
    private final int max_capcity;
    private final int add_switch;

    protected LineManager(Editable s) {
        serveTarget = s;
        pairs = new ArrayList<>();
        garbages = new ArrayList<>();
        zeroPoint = new Pair();
        updateEndPoint();
        max_capcity = 50;
        add_switch = 30;
    }

    private void updateEndPoint() {
        if (endPoint == null) endPoint = new Pair();
        endPoint.first = lineCount - 1;
        final int i = lastIndexOf(serveTarget, serveTarget.length() - 1, '\n');
        if (i == -1) endPoint.first = endPoint.second = 0;
        else endPoint.second = i;
    }

    private void addPair(int line, int index) {
        pairs.add(new Pair(line, index));
        while (pairs.size() > max_capcity) {
            pairs.remove(0);
        }
    }

    private Pair getNearestPair(int line) {
        int bestIndex = -1;
        int bestDistance = line;
        for (int i = pairs.size() - 1; i > -1; i--) {
            Pair pair = pairs.get(i);
            final int newDistance = Math.abs(pair.first - line);
            if (newDistance < bestDistance) {
                bestDistance = newDistance;
                bestIndex = i;
            }
        }
        if (bestIndex != -1) {
            Pair pair = pairs.get(bestIndex);
            pairs.remove(bestIndex);
            pairs.add(pair);
            return pair;
        }
        return zeroPoint;
    }

    private int findForward(Pair pair, int count) {
        int line = pair.first;
        int index = pair.second;
        for (int i = 0; i < count; i++, line++) {
            index = indexOf(serveTarget, (line == 0 ? 0 : index + 1), '\n');
            if (index == -1) return index;
        }
        return index;
    }

    private int findBackward(Pair pair, int count) {
        int line = pair.first;
        int index = pair.second;
        for (int i = 0; i < count; i++, line--) {
            index = lastIndexOf(serveTarget, index - 1, '\n');
        }
        return index;
    }

    private int indexOf(CharSequence s, int start, char c) {
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    private int lastIndexOf(CharSequence s, int start, char c) {
        for (int i = start; i > 0; i--) {
            if (s.charAt(i) == c) {
                return i;
            }
        }
        return -1;
    }

    public final int getLineCount() {
        return lineCount;
    }

    public final int getLineStart(int line) {
        if (line == 0) return 0;
        else if (line < 0 || line >= lineCount)
            throw new IndexOutOfBoundsException("line index out of bounds");
        else {
            final Pair pair = getNearestPairWrapped(line);
            final int distance = line - pair.first;
            if (distance == 0)
                return pair.second;
            else {
                int absDistance = Math.abs(distance);
                int index = distance < 0 ? findBackward(pair, absDistance) : findForward(pair, absDistance);
                if (absDistance >= add_switch) addPair(line, index);
                return index;
            }
        }
    }

    public final int getLineEnd(int line) {
        if (line < 0 || line >= lineCount)
            throw new IndexOutOfBoundsException("line index out of bounds");
        else if (line == getLineCount() - 1)
            return serveTarget.length();
        else return getLineStart(line + 1);
    }

    private Pair getNearestPairByIndex(int index) {
        int i = -1;
        int d = index;
        for (int j = 0; j < pairs.size(); j++) {
            Pair pair = pairs.get(j);
            int nd = Math.abs(index - pair.second);
            if (nd < d) {
                i = j;
                d = nd;
            }
        }
        final Pair pair = (i == -1) ? zeroPoint : pairs.get(i);
        if (i != -1) {
            pairs.remove(pair);
            pairs.add(pair);
        }
        return pair;
    }

    private Pair getNearestPairByIndexWrapped(int index) {
        final Pair pair = getNearestPairByIndex(index);
        final int d = Math.abs(pair.second - index);
        final int td = Math.abs(endPoint.second - index);
        return (d <= td) ? pair : endPoint;
    }

    private Pair getNearestPairWrapped(int line) {
        final Pair pair = getNearestPair(line);
        final int d = Math.abs(pair.first - line);
        final int td = Math.abs(endPoint.first - line);
        return (d <= td) ? pair : endPoint;
    }

    public final int getLineByIndex(int index) {
        if (index == serveTarget.length()) return lineCount - 1;
        else if (index < 0 || index > serveTarget.length())
            throw new StringIndexOutOfBoundsException("index out of bounds");
        final Pair pair = getNearestPairByIndexWrapped(index);
        final int dis = pair.second - index;
        if (dis == 0) return pair.first;
        else return (dis < 0) ? findUtil_Forward(pair, index) : findUtil_Backward(pair, index);
    }

    private int findUtil_Backward(Pair pair, int index) {
        int line = pair.first;
        int i = pair.second;
        final Pair cache = new Pair(0, 0);
        while (i > index) {
            cache.first = line;
            cache.second = i;
            i = findBackward(cache, 1);
            if (i == -1) return 0;
            else line--;
        }
        return line;
    }

    private int findUtil_Forward(Pair pair, int index) {
        int line = pair.first;
        int i = pair.second;
        final Pair cache = new Pair(0, 0);
        while (i < index) {
            cache.first = line;
            cache.second = i;
            i = findForward(cache, 1);
            if (i == -1) return lineCount - 1;
            else line++;
        }
        return line - 1;
    }

    @Override
    public final void onInsert(Editable doc, int index, CharSequence textToInsert) {
        int dL = getNewLineTokenCount(textToInsert);
        lineCount += dL;
        for (Pair pair : pairs) {
            if (index <= pair.second) {
                pair.first += dL;
                pair.second += textToInsert.length();
            }
        }
    }

    @Override
    public final void onDelete(Editable doc, int index, CharSequence textDeleted) {
        int dL = getNewLineTokenCount(textDeleted);
        lineCount -= dL;
        for (Pair pair : pairs) {
            if (index <= pair.second) {
                if (index + textDeleted.length() < pair.second) {
                    pair.first -= dL;
                    pair.second -= textDeleted.length();
                } else {
                    int xDl = getNewLineTokenCount(doc.subSequence(index, pair.second + 1));
                    pair.first -= xDl;
                    try {
                        if (lastIndexOf(serveTarget, index - 1, '\n') == -1) {
                            garbages.add(pair);
                            continue;
                        }
                        int nI = findBackward(new Pair(pair.first, index), 1);
                        if (nI == -1 || pair.first <= 0) garbages.add(pair);
                        pair.second = nI;
                    } catch (Exception e) {
                        e.printStackTrace();
                        garbages.add(pair);
                    }
                }
            }
        }
        pairs.removeAll(garbages);
        garbages.clear();
    }

    @Override
    public final void onReplace(Editable doc) {
    }

    @Override
    public final void onEnd() {
        updateEndPoint();
    }

    private int getNewLineTokenCount(CharSequence s) {
        if (s == null) return 0;
        else {
            int c = 0;
            for (int i = 0; i < s.length(); i++) {
                if (s.charAt(i) == '\n') c++;
            }
            return c;
        }
    }

    private static class Pair {
        public int first;
        public int second;

        public Pair() {
            this(0, 0);
        }

        public Pair(int line, int index) {
            this.first = line;
            this.second = index;
        }
    }
}