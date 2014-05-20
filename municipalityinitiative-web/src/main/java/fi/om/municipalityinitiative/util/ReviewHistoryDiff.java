package fi.om.municipalityinitiative.util;

import com.google.common.collect.Lists;
import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;
import fi.om.municipalityinitiative.dto.service.ReviewHistoryRow;

import java.util.List;

public class ReviewHistoryDiff {
    private Maybe<List<String>> oldText;
    private List<String> newText;
    private List<DiffLine> diff;

    public static ReviewHistoryDiff from(List<ReviewHistoryRow> orderedRows, Long reviewHistoryToDiffToPrevious) {

        List<ReviewHistoryRow> sentRows = filterOnlyWithType(orderedRows, ReviewHistoryType.REVIEW_SENT);
        for (int i = 0; i < sentRows.size(); ++i) {
            if (sentRows.get(i).getId().equals(reviewHistoryToDiffToPrevious)) {
                if (i == sentRows.size() - 1) {
                    return noReviewHistory(sentRows.get(i).getSnapshot().get());
                }
                else {
                    return reviewHistory(sentRows.get(i), sentRows.get(i+1));
                }

            }
        }

        throw new IllegalArgumentException("No reviewhistoryrow with id: " + reviewHistoryToDiffToPrevious);

    }

    private static ReviewHistoryDiff reviewHistory(ReviewHistoryRow newRow, ReviewHistoryRow oldRow) {
        ReviewHistoryDiff reviewHistoryDiff = new ReviewHistoryDiff();
        reviewHistoryDiff.newText = split(newRow.getSnapshot().get());
        reviewHistoryDiff.oldText = Maybe.of(split(oldRow.getSnapshot().get()));
        reviewHistoryDiff.diff = getSomeDiff(DiffUtils.diff(reviewHistoryDiff.oldText.get(), reviewHistoryDiff.newText), reviewHistoryDiff.oldText.get());
        return reviewHistoryDiff;
    }

    private static List<String> split(String s) {
        return Lists.newArrayList(s.split("\n"));
    }

    private static ReviewHistoryDiff noReviewHistory(String s) {
        ReviewHistoryDiff reviewHistoryDiff = new ReviewHistoryDiff();
        reviewHistoryDiff.newText = split(s);
        reviewHistoryDiff.diff = Lists.newArrayList();
        addDiffLines(reviewHistoryDiff.diff, Delta.TYPE.INSERT, reviewHistoryDiff.newText);
        reviewHistoryDiff.oldText = Maybe.absent();
        return reviewHistoryDiff;
    }

    private static List<ReviewHistoryRow> filterOnlyWithType(List<ReviewHistoryRow> orderedRows, ReviewHistoryType type) {
        List<ReviewHistoryRow> filtered = Lists.newArrayList();
        for (ReviewHistoryRow orderedRow : orderedRows) {
            if (orderedRow.getType() == type) {
                filtered.add(orderedRow);
            }
        }
        return filtered;
    }

    public List<DiffLine> getDiff() {
        return diff;
    }

    public Maybe<List<String>> getOldText() {
        return oldText;
    }

    public List<String> getNewText() {
        return newText;
    }

    public static List<DiffLine> getSomeDiff(Patch patch, List<String> oldLines) {

        List<Delta> deltas = patch.getDeltas();

        List<DiffLine> diffLines = Lists.newArrayList();
        Delta currentDelta = popNextDelta(deltas);
        for (int i = 0; i < oldLines.size(); ++i) {
            if (currentDelta == null || currentDelta.getOriginal().getPosition() != i) {
                diffLines.add(new DiffLine(oldLines.get(i)));
            }
            while (currentDelta != null && currentDelta.getOriginal().getPosition() == i) {
                if (currentDelta.getType() == Delta.TYPE.CHANGE) {
                    addDiffLines(diffLines, Delta.TYPE.DELETE, currentDelta.getOriginal().getLines());
                    addDiffLines(diffLines, Delta.TYPE.INSERT, currentDelta.getRevised().getLines());
                } else if (currentDelta.getType() == Delta.TYPE.DELETE) {
                    addDiffLines(diffLines, Delta.TYPE.DELETE, currentDelta.getOriginal().getLines());
                } else if (currentDelta.getType() == Delta.TYPE.INSERT) {
                    addDiffLines(diffLines, Delta.TYPE.INSERT, currentDelta.getRevised().getLines());
                    diffLines.add(new DiffLine(oldLines.get(i)));
                } else {
                    throw new RuntimeException("Unknown delta type: " + currentDelta.getType());
                }

                currentDelta = popNextDelta(deltas);
            }

        }

        return diffLines;
    }

    private static void addDiffLines(List<DiffLine> diffLines, Delta.TYPE type, List<?> lines) {
        for (Object line : lines) {
            diffLines.add(new DiffLine(type, line.toString()));
        }
    }

    private static Delta popNextDelta(List<Delta> deltas) {
        if (deltas.size() == 0) {
            return null;
        }
        else return deltas.remove(0);
    }

    public static class DiffLine {
        final Maybe<Delta.TYPE> modificationType;
        final String line;

        public DiffLine(Delta.TYPE modificationType, String line) {
            this.modificationType = Maybe.of(modificationType);
            this.line = line;
        }

        public DiffLine(String line) {
            this.modificationType = Maybe.absent();
            this.line = line;
        }
    }
}
