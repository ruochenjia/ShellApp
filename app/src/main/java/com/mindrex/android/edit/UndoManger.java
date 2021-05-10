package com.mindrex.android.edit;

import android.text.Editable;

import java.util.ArrayList;

@SuppressWarnings({"unused", "RedundantSuppression"})
public final class UndoManger implements TextWatcher {
    public final static int DEFAULT_MAX_SIZE = 100;
    private boolean replaceFlag;
    private boolean ignoreChange;
    private boolean batchEdit;
    private final ArrayList<Action> stack;
    private int curr;
    private CharSequence cachedText;
    private final int max_size;
    private boolean enabled;

    public UndoManger() {
        this(DEFAULT_MAX_SIZE);
    }

    public UndoManger(int max_size) {
        stack = new ArrayList<>();
        this.max_size = max_size;
        this.curr = -1;
        this.batchEdit = false;
        this.cachedText = null;
        this.ignoreChange = false;
        this.replaceFlag = false;
        this.enabled = true;
    }

    public final void setEnabled(boolean enabled) {
        if (enabled && !this.enabled) clearStack();
        else if (!enabled) this.enabled = false;
    }

    public final boolean isEnabled() {
        return enabled;
    }

    @Override
    public final void onInsert(Editable doc, int index, CharSequence textToInsert) {
        if (!ignoreChange && enabled) {
            if (replaceFlag) {
                stack.add(new ReplaceAction(index, cachedText, textToInsert));
                replaceFlag = false;
            } else add(new InsertAction(index, textToInsert));
        }
    }

    @Override
    public final void onDelete(Editable doc, int index, CharSequence textDeleted) {
        if (!ignoreChange && enabled) {
            if (replaceFlag) cachedText = textDeleted;
            else add(new DeleteAction(index, textDeleted));
        }
    }

    @Override
    public final void onReplace(Editable doc) {
        if (!ignoreChange && enabled) {
            replaceFlag = true;
        }
    }

    public final void setBatchEdit(boolean batchEdit) {
        this.batchEdit = batchEdit;
    }

    public final boolean isBatchEdit() {
        return this.batchEdit;
    }

    private void cleanStackBeforeAdd() {
        while (canRedo()) {
            stack.remove(stack.size() - 1);
        }
    }

    private void cleanStackAfterAdd() {
        while (stack.size() > max_size && curr > 0) {
            stack.remove(0);
            curr--;
        }
    }

    public final void clearStack() {
        curr = -1;
        stack.clear();
        ignoreChange = false;
        replaceFlag = false;
    }

    private void add(Action action) {
        cleanStackBeforeAdd();
        if (batchEdit) {
            if (curr == -1 || !(stack.get(curr) instanceof MultiAction)) {
                stack.add(new MultiAction());
                curr++;
                ((MultiAction) stack.get(curr)).addAction(action);
            } else {
                MultiAction parent = (MultiAction) stack.get(curr);
                if (parent.getLastAction() != null && parent.getLastAction().canMerge(action))
                    parent.getLastAction().merge(action);
                else parent.addAction(action);
            }
            return;
        } else if (curr != -1 && stack.get(curr).canMerge(action))
            stack.get(curr).merge(action);
        else {
            stack.add(action);
            curr++;
        }
        cleanStackAfterAdd();
    }

    public final boolean canUndo() {
        return curr > -1;
    }

    public final boolean canRedo() {
        return curr < stack.size() - 1;
    }

    public final void undo(Editable doc) {
        if (canUndo()) {
            ignoreChange = true;
            stack.get(curr).undo(doc);
            ignoreChange = false;
            curr--;
        }
    }

    public final void redo(Editable doc) {
        if (canRedo()) {
            ignoreChange = true;
            stack.get(curr + 1).redo(doc);
            ignoreChange = false;
            curr++;
        }
    }

    private interface Action {
        void undo(Editable doc);

        void redo(Editable doc);

        boolean canMerge(Action action);

        void merge(Action action);
    }

    private static final class DeleteAction implements Action {
        public int index;
        public CharSequence text;

        public DeleteAction(int i, CharSequence j) {
            index = i;
            text = j;
        }

        @Override
        public final void undo(Editable doc) {
            doc.insert(index, text);
        }

        @Override
        public final void redo(Editable doc) {
            doc.delete(index, text.length());
        }

        @Override
        public final boolean canMerge(Action action) {
            if (action instanceof DeleteAction) {
                final DeleteAction target = (DeleteAction) action;
                return target.index == this.index - text.length();
            } else return false;
        }

        @Override
        public final void merge(Action action) {
            if (canMerge(action)) {
                DeleteAction target = (DeleteAction) action;
                this.index = target.index;
                this.text = target.text.toString() + this.text.toString();
            } else throw new IllegalArgumentException();
        }
    }

    private static final class InsertAction implements Action {
        public int index;
        public CharSequence text;

        public InsertAction(int i, CharSequence j) {
            index = i;
            text = j;
        }

        @Override
        public final void redo(Editable doc) {
            doc.insert(index, text);
        }

        @Override
        public final void undo(Editable doc) {
            doc.delete(index, text.length());
        }

        @Override
        public final boolean canMerge(Action action) {
            if (action instanceof InsertAction) {
                final InsertAction target = (InsertAction) action;
                return target.index == this.index + text.length();
            } else return false;
        }

        @Override
        public final void merge(Action action) {
            if (canMerge(action))
                this.text = this.text.toString() + ((InsertAction) action).text.toString();
            else throw new IllegalArgumentException();
        }
    }

    private static final class ReplaceAction implements Action {
        public int index;
        public CharSequence ori;
        public CharSequence nw;

        public ReplaceAction(int i, CharSequence j, CharSequence k) {
            index = i;
            ori = j;
            nw = k;
        }

        @Override
        public final void undo(Editable doc) {
            doc.delete(index, nw.length());
            doc.insert(index, ori);
        }

        @Override
        public final void redo(Editable doc) {
            doc.delete(index, ori.length());
            doc.insert(index, nw);
        }

        @Override
        public final boolean canMerge(UndoManger.Action action) {
            return false;
        }

        @Override
        public final void merge(UndoManger.Action action) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class MultiAction implements Action {
        private final ArrayList<Action> actions = new ArrayList<>();

        public final void addAction(Action action) {
            actions.add(action);
        }

        public final Action getLastAction() {
            if (actions.size() == 0) return null;
            else return actions.get(actions.size() - 1);
        }

        @Override
        public final void redo(Editable doc) {
            for (int i = actions.size() - 1; i > -1; i--) {
                actions.get(i).redo(doc);
            }
        }

        @Override
        public final void undo(Editable doc) {
            for (int i = actions.size() - 1; i > -1; i--) {
                actions.get(i).undo(doc);
            }
        }

        @Override
        public final boolean canMerge(UndoManger.Action action) {
            return false;
        }

        @Override
        public void merge(UndoManger.Action action) {
            throw new UnsupportedOperationException();
        }
    }
}