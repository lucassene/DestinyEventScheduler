package com.destiny.event.scheduler.data;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class EventTable {

    public static final String TABLE_NAME = "event";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_EN = "en";
    public static final String COLUMN_PT = "pt";
    public static final String COLUMN_ES = "es";
    public static final String COLUMN_ICON = "event_icon";
    public static final String COLUMN_TYPE = "event_type";
    public static final String COLUMN_LIGHT = "event_min_light";
    public static final String COLUMN_GUARDIANS = "event_max_guardians";

    public static final String[] ALL_COLUMNS = {EventTable.COLUMN_ID, EventTable.COLUMN_EN, EventTable.COLUMN_PT, EventTable.COLUMN_ES, EventTable.COLUMN_ICON, EventTable.COLUMN_TYPE, EventTable.COLUMN_LIGHT, EventTable.COLUMN_GUARDIANS};

    private static final String TABLE_CREATE = "CREATE TABLE "
            + TABLE_NAME
            + "("
            + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_EN
            + " TEXT NOT NULL, "
            + COLUMN_PT
            + " TEXT NOT NULL, "
            + COLUMN_ES
            + " TEXT NOT NULL, "
            + COLUMN_ICON
            + " TEXT NOT NULL, "
            + COLUMN_TYPE
            + " INTEGER NOT NULL, "
            + COLUMN_LIGHT
            + " INTEGER NOT NULL, "
            + COLUMN_GUARDIANS
            + " INTEGER NOT NULL"
            + ");";

    public static void onCreate(SQLiteDatabase db){
        db.execSQL(TABLE_CREATE);
        Log.e("EventTable", "Event table created");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS + ")" + " VALUES " + "(null, 'Reciprocal Rune', 'Runa Recíproca', 'Runa Recíproca', 'ic_patrol', '1', 190, 3);");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS + ")" + " VALUES " + "(null, 'Stolen Rune', 'Runa Roubada', 'Runa Robada', 'ic_patrol', '1', 240, 3);");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS + ")" + " VALUES " + "(null, 'Antiquated Rune', 'Runa Antiquada', 'Runa Anticuada', 'ic_patrol', '1', 300, 3);");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Clash', 'Enfrentamento', 'Enfrentamiento', 'ic_clash', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Classic 3x3', 'Clássico 3x3', 'Clásico 3x3', 'ic_classic', '2', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Classic 6x6', 'Clássico 6x6', 'Clásico 6x6', 'ic_clash', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Classic Rumble', 'Clássico: Briga', 'Clásico: Disputa', 'ic_rumble', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Control', 'Controle', 'Control', 'ic_control', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Doubles', 'Duplas', 'Dobles', 'ic_doubles', '2', '5', '2');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Elimination', 'Eliminação', 'Eliminación', 'ic_elimination', '2', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Inferno: Rumble', 'Inferno: Briga', 'Disputa Infernal', 'ic_inferno1', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Inferno: 3x3', 'Inferno: 3x3', 'Infierno: 3x3', 'ic_inferno3v3', '2', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'inferno: 6x6', 'Inferno: 6x6', 'Infierno: 6x6', 'ic_inferno6v6', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Iron Banner', 'Bandeira de Ferro', 'Estandarte de Hierro', 'ic_iron_banner', '2', '230', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Mayhem: Clash', 'Caos: Enfrentamento', 'Caos: Enfrentamiento', 'ic_mayhem_clash', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Mayhem: Rumble', 'Caos: Briga', 'Caos: Disputa', 'ic_mayhem_rumble', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Rift', 'Fissura', 'Grieta', 'ic_rift', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Rumble', 'Briga', 'Disputa', 'ic_rumble', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Salvage', 'Recuperação', 'Rescate', 'ic_salvage', '2', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Skirmish', 'Disputa', 'Escaramuza', 'ic_skirmish', '2', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Trials of Osiris', 'Desafios de Osiris', 'Pruebas de Osiris', 'ic_trials', '2', '251', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Zone Control', 'Controle de Zonas', 'Control de Zonas', 'ic_zone_control', '2', '5', '6');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Patrol the Cosmodrome', 'Patrulhe o Cosmódromo', 'Patrulla el Cosmódromo', 'ic_patrol', '3', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Patrol the Dreadnaught', 'Patrulhe o Encouraçado', 'Patrulla el Acorazado', 'ic_patrol', '3', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Patrol Mars', 'Patrulhe Marte', 'Patrulla Marte', 'ic_patrol', '3', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Patrol the Moon', 'Patrulhe a Lua', 'Patrulla la Luna', 'ic_patrol', '3', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Patrol Venus', 'Patrulhe Vênus', 'Patrulla Venus', 'ic_patrol', '3', '5', '3');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Prison of Elders: 28', 'Prisão dos Anciões: 28', 'El Presidio de los Ancianos: 28', 'ic_prison', '4', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Prison of Elders: 32', 'Prisão dos Anciões: 32', 'El Presidio de los Ancianos: 32', 'ic_prison', '4', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Prison of Elders: 34', 'Prisão dos Anciões: 34', 'El Presidio de los Ancianos: 34', 'ic_prison', '4', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Skola´s Revenge', 'Vingança de Skolas', 'La Venganza de Skolas', 'ic_skolas', '4', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Prison of Elders: 41', 'Prisão dos Anciões: 41', 'El Presidio de los Ancianos: 41', 'ic_prison', '4', '260', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Challenge of Elders', 'Desafio dos Anciões', 'Desafío de los Ancianos', 'ic_challenge', '4', '320', '3');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Crota´s End: Normal', 'O fim de Crota: Normal', 'El fin de Crota: Normal', 'ic_raid', '5', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Crota´s End: Heroic', 'O fim de Crota: Heroico', 'El fin de Crota: Heroico', 'ic_raid', '5', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'King´s Fall: Normal', 'A Queda do Rei: Normal', 'Caída del Rey: Normal',  'ic_raid', '5', '290', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'King´s Fall: Heroic', 'A Queda do Rei: Heroico', 'Caída del Rey: Heroico', 'ic_raid', '5', '310', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Vault of Glass: Normal' , 'Câmara de Cristal: Normal', 'Cámara de Cristal: Normal', 'ic_raid', '5', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Vault of Glass: Heroic' , 'Câmara de Cristal: Heroico', 'Cámara de Cristal: Heroico', 'ic_raid', '5', '5', '6');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Story: Normal', 'História: Normal', 'Historia: Normal', 'ic_story', '6', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Story: Heroic', 'História: Heroica', 'Historia: Heroica', 'ic_story_heroic', '6', '240', '3');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Blighted Chalice', 'Cálice Maculado', 'Cáliz Plagado', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Cerberus Vae III', 'Cerberus Vae III', 'Cerberus Vae III', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Dust Palace', 'Palácio das Areias', 'Palacio de Polvo', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Echo Chamber', 'Câmara do Eco', 'Cámara del Eco', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Fallen S.A.B.E.R.', 'S.A.B.E.R. Decaído', 'S.A.B.E.R. Caído', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Shield Brothers', 'Irmãos Escudeiros', 'Hermanos Escudo', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Devil´s Lair', 'Covil dos Demônios', 'Guarida de los Demonios', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'The Nexus', 'O Nexo', 'El Nexo', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Shadow Thief', 'O Ladrão das Sombras', 'El Ladrón de Sombras', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'The Summoning Pits', 'O Precipício da Invocação', 'Los Fosos de Invocación', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'The Sunless Cell', 'A Cela sem Sol', 'La Celda sin Sol', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'The Undying Mind', 'A Mente Imortal', 'La mente Imperecedera', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Will of Crota', 'A Vontade de Crota', 'La Voluntad de Crota', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Winter´s Run', 'Corrida Invernal', 'Carrera de Invierno', 'ic_strike', '7', '5', '3');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Nightfall Strike', 'Assalto do Anoitecer', 'Asalto de Ocaso', 'ic_nightfall', '8', '280', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Weekly Heroic Strike', 'Assalto Heroico Semanal', 'Asalto Heroico Semanal', 'ic_weekly_strike', '8', '260', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Taken War: Heroic', 'Guerra dos Possuídos: Heroico', 'Guerra de los Possuídos: Heroico', 'ic_strike_heroic', '8', '260', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Taken War', 'Guerra dos Possuídos', 'Guerra de los Possuídos', 'ic_strike', '8', '200', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Legacy Strikes', 'Assaltos do Legado', 'Legados de la Vanguarda', 'ic_strike', '8', '5', '3');");

        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Sparrow Race', 'Corrida de Pardais', 'Carreras de Colibriés', 'ic_srl', '9', '5', '6');");

        //Version 1.04
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Inferno: Doubles', 'Inferno: Duplas', 'Infierno: Dobles', 'ic_inferno2', '2', '5', '2');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Supremacy', 'Supremacia', 'Supremacía', 'ic_supremacy', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Inferno: Supremacy', 'Inferno: Supremacia', 'Supremacía Infernal', 'ic_inferno_sup', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Rumble Supremacy', 'Briga: Supremacia', 'Disputa: Supremacía', 'ic_rumble_sup', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Mayhem: Supremacy', 'Caos: Supremacia', 'Caos: Supremacía', 'ic_supremacy', '2', '5', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'The Abomination Heist', 'O Golpe à Abominação', 'El Rapto de la Abominación', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Sepiks Perfected', 'Sepiks Aperfeiçoado', 'Sepiks Perfeccionado', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'The Wretched Eye', 'O Olho Maldito', 'El Abominable Ojo', 'ic_strike', '7', '5', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Patrol the Plaguelands', 'Patrulhe as Terras Pestíferas', 'Patrulla las Tierras Pestíferas', 'ic_patrol', '3', '320', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'SIVA Crisis', 'Crise da SIVA', 'Crisis de SIVA', 'ic_strike', '8', '320', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'SIVA Crisis: Heroic', 'Crise da SIVA: Heroico', 'Crisis de SIVA Heroica', 'ic_strike', '8', '350', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Combined Arms', 'Armas Combinadas', 'Armas Combinadas', 'ic_combined', '2', '5', '6');");

        //Version 1.05
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Wrath of the Machine: Normal', 'A Ira da Máquina: Normal', 'La Fúria de las Máquinas: Normal', 'ic_raid', '5', '370', '6');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Fused Offering', 'Oferenda Fundida', 'Ofrenda Imbuida', 'ic_patrol', '10', '320', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Enhanced Offering', 'Oferenda Otimizada', 'Ofrenda Alterada', 'ic_patrol', '10', '340', '3');");
        db.execSQL("INSERT INTO " + TABLE_NAME + "(" + COLUMN_ID + ", " + COLUMN_EN + ", " + COLUMN_PT + ", " + COLUMN_ES + ", " + COLUMN_ICON + ", " + COLUMN_TYPE + ", " + COLUMN_LIGHT + ", " + COLUMN_GUARDIANS  + ")" + " VALUES " + "(null, 'Perfected Offering', 'Oferenda Aperfeiçoada', 'Ofrenda Perfeccionada', 'ic_patrol', '10', '360', '3');");
    }

    static void onUpdate(SQLiteDatabase db, int oldVersion, int newVersion){
        Log.e(EventTable.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public static String getName(Context context, Cursor cursor){
        context.getResources();
        switch (Resources.getSystem().getConfiguration().locale.getLanguage()){
            case "pt":
                return cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_PT));
            case "es":
                return cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_ES));
            default:
                return cursor.getString(cursor.getColumnIndexOrThrow(EventTypeTable.COLUMN_EN));
        }
    }

}
