#!/usr/bin/env python3
"""generate the java calendar table from data/calendar_data.json."""

from __future__ import annotations

import json
import pathlib
import sys

ROOT = pathlib.Path(__file__).resolve().parent.parent
DEFAULT_SRC = ROOT / "data" / "calendar_data.json"
DEFAULT_OUT = ROOT / "src" / "com" / "harilvfs" / "npltz" / "CalendarData.java"


def fail(message: str) -> "NoReturn":
    print(f"gen_calendar_data: {message}", file=sys.stderr)
    raise SystemExit(1)


def main() -> None:
    src = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_SRC
    out = pathlib.Path(sys.argv[2]) if len(sys.argv) > 2 else DEFAULT_OUT

    try:
        rows = json.loads(src.read_text(encoding="utf-8"))
    except OSError as exc:
        fail(f"cannot read {src}: {exc}")
    except json.JSONDecodeError as exc:
        fail(f"invalid JSON in {src}: {exc}")

    if not isinstance(rows, list) or not rows:
        fail("calendar data must be a non-empty list")

    rows.sort(key=lambda item: item["year"])
    first = rows[0]["year"]
    years = [row["year"] for row in rows]
    if years != list(range(first, first + len(rows))):
        fail("calendar years must be contiguous")

    month_lines = []
    total_lines = []
    for row in rows:
        year = row["year"]
        months = row["months"]
        total = row["total_days"]
        if len(months) != 12:
            fail(f"{year}: expected 12 months, found {len(months)}")
        if any(not isinstance(days, int) or days <= 0 for days in months):
            fail(f"{year}: month lengths must be positive integers")
        if total != sum(months):
            fail(f"{year}: total_days {total} != sum(months) {sum(months)}")
        month_lines.append("        {" + ", ".join(str(days) for days in months) + "},")
        total_lines.append(f"        {total},")

    text = f"""\
// generated from data/calendar_data.json; do not edit.
package com.harilvfs.npltz;

final class CalendarData {{
    static final int BS_EPOCH_YEAR = {first};
    static final int BS_MAX_YEAR = {years[-1]};

    private static final int[][] MONTHS = {{
{chr(10).join(month_lines)}
    }};

    private static final int[] TOTALS = {{
{chr(10).join(total_lines)}
    }};

    private CalendarData() {{}}

    static int[] months(int bsYear) {{
        int index = bsYear - BS_EPOCH_YEAR;
        if (index < 0 || index >= MONTHS.length) {{
            return null;
        }}
        return MONTHS[index];
    }}

    static int totalDays(int bsYear) {{
        int index = bsYear - BS_EPOCH_YEAR;
        if (index < 0 || index >= TOTALS.length) {{
            return 0;
        }}
        return TOTALS[index];
    }}
}}
"""
    out.parent.mkdir(parents=True, exist_ok=True)
    out.write_text(text, encoding="utf-8")
    print(f"generated {out} ({len(rows)} years, {first}-{years[-1]} BS)")


if __name__ == "__main__":
    main()
