/*
 *  Copyright 2017 TWO SIGMA OPEN SOURCE, LLC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.twosigma.beaker.table;

import com.twosigma.beaker.KernelTest;
import com.twosigma.beaker.chart.xychart.XYChart;
import com.twosigma.beaker.jupyter.KernelManager;
import com.twosigma.beaker.table.format.TableDisplayStringFormat;
import com.twosigma.beaker.table.renderer.TableDisplayCellRenderer;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.twosigma.beaker.table.serializer.ObservableTableDisplaySerializer.DOUBLE_CLICK_TAG;
import static com.twosigma.beaker.table.serializer.ObservableTableDisplaySerializer.HAS_DOUBLE_CLICK_ACTION;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.ALIGNMENT_FOR_COLUMN;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.ALIGNMENT_FOR_TYPE;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.COLUMNS_FROZEN;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.COLUMNS_FROZEN_RIGHT;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.COLUMNS_VISIBLE;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.COLUMN_ORDER;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.DATA_FONT_SIZE;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.HAS_INDEX;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.HEADERS_VERTICAL;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.HEADER_FONT_SIZE;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.RENDERER_FOR_COLUMN;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.RENDERER_FOR_TYPE;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.STRING_FORMAT_FOR_COLUMN;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.STRING_FORMAT_FOR_TIMES;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.STRING_FORMAT_FOR_TYPE;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.TABLE_DISPLAY;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.TIME_ZONE;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.TYPE;
import static com.twosigma.beaker.table.serializer.TableDisplaySerializer.VALUES;
import static com.twosigma.beaker.widgets.TestWidgetUtils.findValueForProperty;
import static com.twosigma.beaker.widgets.TestWidgetUtils.getValueForProperty;
import static com.twosigma.beaker.widgets.TestWidgetUtils.verifyOpenCommMsgWitoutLayout;
import static org.assertj.core.api.Assertions.assertThat;

public class TableDisplayTest {

  public static final String COL_1 = "str1";
  protected KernelTest kernel;

  private TableDisplay tableDisplay;

  @Before
  public void setUp() throws Exception {
    kernel = new KernelTest();
    KernelManager.register(kernel);
    tableDisplay = new TableDisplay(getListOfMapsData());
    kernel.clearSentMessages();
    kernel.clearPublishedMessages();
  }

  @After
  public void tearDown() throws Exception {
    KernelManager.register(null);
  }

  @Test
  public void shouldSendCommMsgWhenAlignmentProviderForColumnChange() throws Exception {
    //given
    TableDisplayAlignmentProvider centerAlignment = TableDisplayAlignmentProvider.CENTER_ALIGNMENT;
    //when
    tableDisplay.setAlignmentProviderForColumn(COL_1, centerAlignment);
    //then
    assertThat(tableDisplay.getAlignmentForColumn().get(COL_1)).isEqualTo(centerAlignment);
    LinkedHashMap model = getModel();
    assertThat(model.get(ALIGNMENT_FOR_COLUMN)).isNotNull();
  }

  @Test
  public void shouldSendCommMsgWhenAlignmentProviderForTypeChange() throws Exception {
    //given
    TableDisplayAlignmentProvider centerAlignment = TableDisplayAlignmentProvider.CENTER_ALIGNMENT;
    //when
    tableDisplay.setAlignmentProviderForType(ColumnType.String, centerAlignment);
    //then
    assertThat(tableDisplay.getAlignmentForType().get(ColumnType.String)).isEqualTo(centerAlignment);
    LinkedHashMap model = getModel();
    assertThat(model.get(ALIGNMENT_FOR_TYPE)).isNotNull();
  }

  @Test
  public void shouldSendCommMsgWhenColumnFrozenChange() throws Exception {
    //given
    //when
    tableDisplay.setColumnFrozen(COL_1, true);
    //then
    assertThat(tableDisplay.getColumnsFrozen().get(COL_1)).isEqualTo(true);
    LinkedHashMap model = getModel();
    assertThat(model.get(COLUMNS_FROZEN)).isNotNull();
  }

  @Test
  public void shouldSendCommMsgWhenColumnFrozenRightChange() throws Exception {
    //given
    //when
    tableDisplay.setColumnFrozenRight(COL_1, true);
    //then
    assertThat(tableDisplay.getColumnsFrozenRight().get(COL_1)).isEqualTo(true);
    LinkedHashMap model = getModel();
    assertThat(model.get(COLUMNS_FROZEN_RIGHT)).isNotNull();
  }

  @Test
  public void shouldSendCommMsgWhenColumnOrderChange() throws Exception {
    //given
    //when
    tableDisplay.setColumnOrder(getStringList());
    //then
    assertThat(tableDisplay.getColumnOrder()).isEqualTo(getStringList());
    LinkedHashMap model = getModel();
    assertThat(model.get(COLUMN_ORDER)).isEqualTo(getStringList());
  }

  @Test
  public void shouldSendCommMsgWhenColumnVisibleChange() throws Exception {
    //given
    //when
    tableDisplay.setColumnVisible(COL_1, true);
    //then
    assertThat(tableDisplay.getColumnsVisible().get(COL_1)).isEqualTo(true);
    assertThat(getValueAsMap(COLUMNS_VISIBLE).get(COL_1)).isEqualTo(true);
  }

  @Test
  public void shouldSendCommMsgWhenDataFontSizeChange() throws Exception {
    //given
    //when
    tableDisplay.setDataFontSize(12);
    //then
    assertThat(tableDisplay.getDataFontSize()).isEqualTo(12);
    LinkedHashMap model = getModel();
    assertThat(model.get(DATA_FONT_SIZE)).isEqualTo(12);
  }


  @Test
  public void shouldSendCommMsgWhenDoubleClickActionChange() throws Exception {
    //given
    //when
    tableDisplay.setDoubleClickAction(new Object());
    //then
    assertThat(tableDisplay.hasDoubleClickAction()).isEqualTo(true);
    LinkedHashMap model = getModel();
    assertThat(model.get(HAS_DOUBLE_CLICK_ACTION)).isEqualTo(true);
  }

  @Test
  public void shouldSendCommMsgWhenDoubleClickActionTagChange() throws Exception {
    //given
    String clickTag = "ClickTag";
    //when
    tableDisplay.setDoubleClickAction(clickTag);
    //then
    assertThat(tableDisplay.getDoubleClickTag()).isEqualTo(clickTag);
    LinkedHashMap model = getModel();
    assertThat(model.get(DOUBLE_CLICK_TAG)).isEqualTo(clickTag);
  }

  @Test
  public void shouldSendCommMsgWhenHasIndexChange() throws Exception {
    //given
    String index1 = "index1";
    //when
    tableDisplay.setHasIndex(index1);
    //then
    assertThat(tableDisplay.getHasIndex()).isEqualTo(index1);
    LinkedHashMap model = getModel();
    assertThat(model.get(HAS_INDEX)).isEqualTo(index1);
  }

  @Test
  public void shouldSendCommMsgWhenHeaderFontSizeChange() throws Exception {
    //given
    //when
    tableDisplay.setHeaderFontSize(12);
    //then
    assertThat(tableDisplay.getHeaderFontSize()).isEqualTo(12);
    LinkedHashMap model = getModel();
    assertThat(model.get(HEADER_FONT_SIZE)).isEqualTo(12);
  }

  @Test
  public void shouldSendCommMsgWhenHeadersVerticalChange() throws Exception {
    //given
    //when
    tableDisplay.setHeadersVertical(true);
    //then
    assertThat(tableDisplay.getHeadersVertical()).isEqualTo(true);
    LinkedHashMap model = getModel();
    assertThat(model.get(HEADERS_VERTICAL)).isEqualTo(true);
  }

  @Test
  public void shouldSendCommMsgWhenRendererForColumnChange() throws Exception {
    //given
    TableDisplayCellRenderer dataBarsRenderer = TableDisplayCellRenderer.getDataBarsRenderer();
    //when
    tableDisplay.setRendererForColumn(COL_1, dataBarsRenderer);
    //then
    assertThat(tableDisplay.getRendererForColumn().get(COL_1)).isEqualTo(dataBarsRenderer);
    LinkedHashMap model = getModel();
    assertThat(model.get(RENDERER_FOR_COLUMN)).isNotNull();
  }

  @Test
  public void shouldSendCommMsgWhenRendererForTypeChange() throws Exception {
    //given
    TableDisplayCellRenderer dataBarsRenderer = TableDisplayCellRenderer.getDataBarsRenderer();
    //when
    tableDisplay.setRendererForType(ColumnType.String, dataBarsRenderer);
    //then
    assertThat(tableDisplay.getRendererForType().get(ColumnType.String)).isEqualTo(dataBarsRenderer);
    LinkedHashMap model = getModel();
    assertThat(model.get(RENDERER_FOR_TYPE)).isNotNull();
  }

  @Test
  public void shouldSendCommMsgWhenStringFormatForColumnChange() throws Exception {
    //given
    TableDisplayStringFormat timeFormat = TableDisplayStringFormat.getTimeFormat();
    //when
    tableDisplay.setStringFormatForColumn(COL_1, timeFormat);
    //then
    assertThat(tableDisplay.getStringFormatForColumn().get(COL_1)).isEqualTo(timeFormat);
    LinkedHashMap model = getModel();
    assertThat(model.get(STRING_FORMAT_FOR_COLUMN)).isNotNull();
  }

  @Test
  public void shouldSendCommMsgWhenStringFormatForTimesChange() throws Exception {
    //given
    TimeUnit days = TimeUnit.DAYS;
    //when
    tableDisplay.setStringFormatForTimes(days);
    //then
    assertThat(tableDisplay.getStringFormatForTimes()).isEqualTo(days);
    LinkedHashMap model = getModel();
    assertThat(model.get(STRING_FORMAT_FOR_TIMES)).isEqualTo(days.toString());
  }

  @Test
  public void shouldSendCommMsgWhenStringFormatForTypeChange() throws Exception {
    //given
    TableDisplayStringFormat timeFormat = TableDisplayStringFormat.getTimeFormat();
    //when
    tableDisplay.setStringFormatForType(ColumnType.String, timeFormat);
    //then
    assertThat(tableDisplay.getStringFormatForType()).isNotNull();
    LinkedHashMap model = getModel();
    assertThat(model.get(STRING_FORMAT_FOR_TYPE)).isNotNull();
  }

  @Test
  public void shouldSendCommMsgWhenTimeZoneChange() throws Exception {
    //given
    String timezone = "TZ1";
    //when
    tableDisplay.setTimeZone(timezone);
    //then
    assertThat(tableDisplay.getTimeZone()).isEqualTo(timezone);
    LinkedHashMap model = getModel();
    assertThat(model.get(TIME_ZONE)).isEqualTo(timezone);
  }

  @Test
  public void shouldSendCommMsgWhenValuesChange() throws Exception {
    //given
    kernel.clearPublishedMessages();
    kernel.clearSentMessages();
    ArrayList<Map<?, ?>> v = new ArrayList<>();
    //when
    TableDisplay tableDisplay =  new TableDisplay(v);
    //then
    verifyOpenCommMsgWitoutLayout(kernel.getPublishedMessages(),TableDisplay.MODEL_NAME_VALUE,TableDisplay.VIEW_NAME_VALUE);
    Map valueForProperty = getValueForProperty(kernel.getPublishedMessages().get(1), TableDisplay.MODEL, Map.class);
    assertThat(valueForProperty.get(TYPE)).isEqualTo(TABLE_DISPLAY);
    assertThat(tableDisplay.getValues()).isEqualTo(v);
    LinkedHashMap model = getModel();
    assertThat(model.get(VALUES)).isNotNull();
  }

  @Test
  public void createWithListOfMapsParam_hasListOfMapsSubtype() throws Exception {
    //when
    TableDisplay tableDisplay = new TableDisplay(getListOfMapsData());
    //then
    Assertions.assertThat(tableDisplay.getSubtype()).isEqualTo(TableDisplay.LIST_OF_MAPS_SUBTYPE);
    Assertions.assertThat(tableDisplay.getValues().size()).isEqualTo(2);
    Assertions.assertThat(tableDisplay.getColumnNames().size()).isEqualTo(3);
    Assertions.assertThat(tableDisplay.getTypes().size()).isEqualTo(3);
  }

  @Test
  public void createWithListsParams_hasTableDisplaySubtype() throws Exception {
    //when
    TableDisplay tableDisplay =
            new TableDisplay(Arrays.asList(getRowData(), getRowData()), getStringList(), getStringList());
    //then
    Assertions.assertThat(tableDisplay.getSubtype()).isEqualTo(TableDisplay.TABLE_DISPLAY_SUBTYPE);
    Assertions.assertThat(tableDisplay.getValues().size()).isEqualTo(2);
    Assertions.assertThat(tableDisplay.getColumnNames().size()).isEqualTo(3);
    Assertions.assertThat(tableDisplay.getTypes().size()).isEqualTo(3);
  }

  @Test
  public void createTableDisplayForMap_hasDictionarySubtype() throws Exception {
    //when
    TableDisplay tableDisplay = new TableDisplay(getMapData());
    //then
    Assertions.assertThat(tableDisplay.getSubtype()).isEqualTo(TableDisplay.DICTIONARY_SUBTYPE);
    Assertions.assertThat(tableDisplay.getValues().size()).isEqualTo(3);
    Assertions.assertThat(tableDisplay.getColumnNames().size()).isEqualTo(2);
    Assertions.assertThat(tableDisplay.getTypes().size()).isEqualTo(0);
  }

  @Test
  public void createTableDisplay_hasCommIsNotNull() throws Exception {
    //when
    TableDisplay tableDisplay = new TableDisplay(getListOfMapsData());
    //then
    Assertions.assertThat(tableDisplay.getComm()).isNotNull();
  }

  @Test
  public void getValuesAsRowsWithoutParams_returnedListOfMapsIsNotEmpty() throws Exception {
    //given
    TableDisplay tableDisplay = new TableDisplay(getListOfMapsData());
    //when
    List<Map<String, Object>> rows = tableDisplay.getValuesAsRows();
    //then
    Assertions.assertThat(rows.size()).isEqualTo(2);
    Assertions.assertThat(rows.get(0).size()).isEqualTo(3);
  }

  @Test
  public void getValuesAsRowsWithTwoParams_returnedListOfMapsIsNotEmpty() throws Exception {
    //when
    List<Map<String, Object>> rows =
            TableDisplay.getValuesAsRows(Arrays.asList(getRowData(), getRowData()), getStringList());
    //then
    Assertions.assertThat(rows.size()).isEqualTo(2);
    Assertions.assertThat(rows.get(0).size()).isEqualTo(3);
  }

  @Test
  public void getValuesAsMatrixWithoutParams_returnedListOfListIsNotEmpty() throws Exception {
    //given
    TableDisplay tableDisplay = new TableDisplay(getListOfMapsData());
    //when
    List<List<?>> values = tableDisplay.getValuesAsMatrix();
    //then
    Assertions.assertThat(values).isNotEmpty();
  }

  @Test
  public void getValuesAsMatrixWithParam_returnedListOfListIsNotEmpty() throws Exception {
    //when
    List<List<?>> values =
            TableDisplay.getValuesAsMatrix(Arrays.asList(getStringList(), getRowData()));
    //then
    Assertions.assertThat(values).isNotEmpty();
  }

  @Test
  public void getValuesAsDictionaryWithoutParam_returnedMapIsNotEmpty() throws Exception {
    //given
    TableDisplay tableDisplay = new TableDisplay(getMapData());
    //when
    Map<String, Object> dictionary = tableDisplay.getValuesAsDictionary();
    //then
    Assertions.assertThat(dictionary).isNotEmpty();
  }

  @Test
  public void getValuesAsDictionaryWithParam_returnedMapIsNotEmpty() throws Exception {
    //when
    Map<String, Object> dictionary =
            TableDisplay.getValuesAsDictionary(Arrays.asList(Arrays.asList("k1", 1), Arrays.asList("k2", 2)));
    //then
    Assertions.assertThat(dictionary).isNotEmpty();
  }

  private List<Map<?, ?>> getListOfMapsData() {
    List<Map<?, ?>> list = new ArrayList<>();
    List<String> cols = getStringList();
    List<?> row = getRowData();
    list.add(
            new HashMap<String, Object>() {
              {
                put(cols.get(0), row.get(0));
                put(cols.get(1), row.get(1));
                put(cols.get(2), row.get(2));
              }
            });
    list.add(
            new HashMap<String, Object>() {
              {
                put(cols.get(0), row.get(0));
                put(cols.get(1), row.get(1));
                put(cols.get(2), row.get(2));
              }
            });
    return list;
  }

  private Map<?, ?> getMapData() {
    return new HashMap<String, Object>() {
      {
        put("key1", 1);
        put("key2", 2);
        put("key3", 3);
        put("key1", 4);
        put("key2", 5);
      }
    };
  }

  private List<String> getStringList() {
    return Arrays.asList(COL_1, "str2", "str3");
  }

  private List<?> getRowData() {
    return Arrays.asList(new Float(1.0), 1490970521000L, "value1");
  }

  protected LinkedHashMap getModel() {
    return findValueForProperty(kernel, XYChart.MODEL, LinkedHashMap.class);
  }

  protected Map getValueAsMap(final String field) {
    LinkedHashMap model = getModel();
    return (Map) model.get(field);
  }

}
