#!/usr/bin/env node

import https from 'https';
import {resolve, dirname} from 'path';
import {readFile, readdir, writeFile} from 'fs/promises';

const scriptFilePath = new URL(import.meta.url).pathname;

const projectDirectory = resolve(dirname(scriptFilePath), '..');
const sourceDirectory = resolve(projectDirectory, 'app/src/main/java/de/westnordost/streetcomplete/');

const csvFilePath = resolve(projectDirectory, 'quest-list.csv');

const noteQuestName = 'OsmNoteQuest';
const noteQuestPath = resolve(sourceDirectory, 'data/osmnotes/notequests/OsmNoteQuestType.kt');

const wikiRowSpan2 = ' rowspan="2" |';


/** @type string[] */
let questFiles;

/** @type Record<string, string> */
let strings;

/** @type WikiTableQuest[] */
let wikiTable;

/** @type RepoQuest[] */
let quests;


(async () => {
    const questFile = await readFile(resolve(sourceDirectory, 'quests/QuestModule.kt'), 'utf8');
    const questNames = questFile.match(/(?<=^ {8})[A-Z][a-zA-Z]+(?=\()/gm);
    questNames.unshift(noteQuestName);

    questFiles = await getFiles(resolve(sourceDirectory, 'quests/'));
    strings = await getStrings(resolve(projectDirectory, 'app/src/main/res/values/strings.xml'));

    const wikiPageContent = await getWikiTableContent();
    wikiTable = parseWikiTable(wikiPageContent);

    quests = await Promise.all(questNames.map((name, defaultPriority) => getQuest(name, defaultPriority)));

    quests.sort((a, b) => a.wikiOrder - b.wikiOrder);

    await writeCsvFile();
})().catch(error => {
    console.error(error);
    process.exit(1);
});


class RepoQuest {
    /**
     * @param {string} name
     * @param {string} filePath
     * @param {string} title
     * @param {number} defaultPriority
     * @param {number} wikiOrder
     */
    constructor(name, filePath, title, defaultPriority, wikiOrder) {
        /** @type string */
        this.name = name;

        /** @type string */
        this.filePath = filePath;

        /** @type string */
        this.title = title;

        /** @type number */
        this.defaultPriority = defaultPriority;

        /** @type number */
        this.wikiOrder = wikiOrder;
    }

    get csvString() {
        const wikiOrder = this.wikiOrder === -1 ? '???' : this.wikiOrder;
        return `"${this.name}", "${this.title}", ${this.defaultPriority + 1}, ${wikiOrder}`;
    }
}

class WikiTableQuest {
    /** @type number */
    wikiOrder;

    /** @type string */
    icon;

    /** @type string */
    question;

    /** @type string */
    askedForElements;

    /** @type string */
    modifiedTags;

    /** @type string */
    defaultPriority;

    /** @type string */
    sinceVersion;

    /** @type string */
    notes;

    /** @type string */
    code;

    /**
     * @param {string[]} rowCells
     * @param {number} rowIndex
     */
    constructor(rowCells, rowIndex) {
        this.wikiOrder = rowIndex;

        const tableColumns = ['icon', 'question', 'askedForElements', 'modifiedTags', 'defaultPriority', 'sinceVersion', 'notes', 'code'];

        for (let [cellIndex, cell] of rowCells.entries()) {
            if (/^ ?(?:rowspan=|colspan=)/.test(cell)) {
                if (cell.startsWith(wikiRowSpan2)) {
                    cell = cell.slice(wikiRowSpan2.length);
                }
                else {
                    throw new Error(`Unsupported rowspan > 2 or colspan detected in table row ${rowIndex}: ${cell}`);
                }
            }

            const column = tableColumns[cellIndex];
            this[column] = cell.trim();
        }
    }

    get isOutdated() {
        return !quests.some(quest => quest.wikiOrder === this.wikiOrder);
    }

    get csvString() {
        return `"???", "${this.question}", "???", ${this.wikiOrder}`;
    }
}


/**
 * @param {string} directory
 * @returns {Promise<string[]>} A list of all files (searched recursively) in the given directory.
 */
async function getFiles(directory) {
    const entries = await readdir(directory, { withFileTypes: true });

    return (await Promise.all(
        entries.map(entry => {
            const resolved = resolve(directory, entry.name);
            return entry.isDirectory() ? getFiles(resolved) : resolved;
        }),
    )).flat();
}


/**
 * @param {string} stringsFileName
 * @returns {Promise<Record<string, string>>} An object with all Android String Resource names and their respective values from the given file.
 */
async function getStrings(stringsFileName) {
    const stringRegex = /<string name="([^"]+)">(.*?)<\/string>/gs;

    const normalizeString = string =>
        string.replace(/^"/, '').replace(/"$/, '')  // strip optional quotes around the string
            .replace(/\\n/g, '\n')                  // replace \n with real newline characters
            .replace(/\\(['"])/g, '$1');            // unescape quotes

    const stringsContent = await readFile(stringsFileName, 'utf8');

    return Object.fromEntries(
        stringsContent.match(stringRegex).map(singleString => {
            const [, name, value] = new RegExp(stringRegex).exec(singleString);
            return [name, normalizeString(value)];
        }),
    );
}

/**
 * Simplified polyfill for
 * https://developer.mozilla.org/en-us/docs/Web/JavaScript/Reference/Global_Objects/String/replaceAll
 * @param {string} search
 * @param {string} replace
 * @returns {string}
 */
String.prototype.replaceAll = function(search, replace) {
    return this.split(search).join(replace);
}

/**
 * @param {string} questName
 * @param {number} defaultPriority
 * @returns {RepoQuest} All information about the quest with the given name.
 */
async function getQuest(questName, defaultPriority) {
    const filePath = getQuestFilePath(questName);
    const questFileContent = await readFile(filePath, 'utf8');

    const questions = getQuestTitleStringNames(questName, questFileContent).map(
        name => strings[name].replace(/%s|%\d\$s/g, '[…]').replaceAll('([…])', '[…]').replaceAll('[…] […]', '[…]'),
    );

    const wikiOrder = wikiTable.findIndex(tableRow => questions.includes(tableRow.question));
    const title = wikiOrder > -1 ? wikiTable[wikiOrder].question : questions.pop();

    return new RepoQuest(questName, filePath, title, defaultPriority, wikiOrder);
}


/**
 * @param {string} questName
 * @returns {string} The absolute path of the quest's file.
 */
function getQuestFilePath(questName) {
    if (questName === noteQuestName) {
        return noteQuestPath;
    }

    const questFile = questFiles.find(path => path.endsWith(questName + '.kt'));

    if (!questFile) {
        throw new Error(`Could not find quest file for quest '${questName}'.`);
    }

    return questFile;
}


/**
 * @param {string} questName
 * @param {string} questFileContent
 * @returns {string} The Android String Resource name of the quest's title.
 */
function getQuestTitleStringNames(questName, questFileContent) {
    let stringResourceNames = questFileContent.match(/(?<=R\.string\.)quest_\w+/g);

    if (stringResourceNames.length === 0) {
        throw new Error(`Could not find the title string reference for quest '${questName}'.`);
    }

    if (stringResourceNames.length === 1) {
        return stringResourceNames;
    }

    return stringResourceNames.filter(name => name.includes('title'));
}


/**
 * @returns {Promise<string>} The quest list wiki page content as a string.
 */
async function getWikiTableContent() {
    const apiUrl = new URL('https://wiki.openstreetmap.org/w/api.php');
    apiUrl.searchParams.set('action', 'parse');
    apiUrl.searchParams.set('format', 'json');
    apiUrl.searchParams.set('prop', 'wikitext');
    apiUrl.searchParams.set('formatversion', '2');
    apiUrl.searchParams.set('page', 'StreetComplete/Quests');
    apiUrl.searchParams.set('section', '1'); // "Released quest types" section

    return new Promise((resolve, reject) => {
        https.get(apiUrl, response => {
            let data = '';

            response.on('data', chunk => {
                data += chunk;
            });

            response.on('end', () => {
                resolve(JSON.parse(data).parse.wikitext);
            });
        }).on('error', error => {
            reject(error);
        });
    });
}


/**
 * @param {string} wikiPageContent
 * @returns {WikiTableQuest[]}
 */
function parseWikiTable(wikiPageContent) {
    const tableRows = wikiPageContent.split('|-');

    tableRows.shift(); // Drop table header and everything before the table
    tableRows.push(tableRows.pop().split('|}')[0]); // Drop everything after the table

    const cells = [];

    for (const [rowIndex, row] of tableRows.entries()) {
        const rowCells = row.split('\n|').slice(1);

        if (rowIndex > 0) {
            const previousRowCells = cells[rowIndex - 1];
            for (const [cellIndex, cell] of previousRowCells.entries()) {
                if (cell.startsWith(wikiRowSpan2)) {
                    rowCells.splice(cellIndex, 0, cell.slice(wikiRowSpan2.length));
                }
            }
        }

        cells.push(rowCells);
    }

    return cells.map((rowCells, rowIndex) => new WikiTableQuest(rowCells, rowIndex));
}


async function writeCsvFile() {
    const newQuests = quests.filter(quest => quest.wikiOrder === -1);
    const oldQuests = quests.filter(quest => quest.wikiOrder > -1);

    const csvLines = [
        '"Quest Name", "Question", "Default Priority", "Wiki Order"',
        ...wikiTable.filter(quest => quest.isOutdated).map(quest => quest.csvString),
        ',,,',
        ...newQuests.map(quest => quest.csvString),
        ',,,',
        ...oldQuests.map(quest => quest.csvString),
    ];

    await writeFile(csvFilePath, csvLines.join('\n'));
}
