package com.barda.sdk.plugin.sqlcommand.changeset;

import com.barda.sdk.util.SqlGuiUtils.GuiSqlValue;

public record ChangeSetItem(String column, GuiSqlValue guiSqlValue) {
}