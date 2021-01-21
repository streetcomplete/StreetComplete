#!/usr/bin/env node

import {resolve, dirname, relative} from 'path';
import {readFile, readdir, writeFile} from 'fs/promises';
import {execFileSync} from 'child_process';

const scriptFilePath = new URL(import.meta.url).pathname;

const projectDirectory = resolve(dirname(scriptFilePath), '..');
const sourceDirectory = resolve(projectDirectory, 'app/src/main/java/de/westnordost/streetcomplete/');
const iconsDirectory = resolve(projectDirectory, 'res/graphics/quest icons/');

const markdownFilePath = resolve(projectDirectory, 'quests.md');

const noteQuestName = 'OsmNoteQuest';
const noteQuestPath = resolve(sourceDirectory, 'data/osmnotes/notequests/OsmNoteQuestType.kt');


/** @type string[] */
let questNames;

/** @type string[] */
let questFiles;

/** @type Record<string, string> */
let strings;


(async () => {
    const questFile = await readFile(resolve(sourceDirectory, 'quests/QuestModule.kt'), 'utf8');
    questNames = questFile.match(/(?<=^ {8})[A-Z][a-zA-Z]+(?=\()/gm);
    const sortedQuestNames = questNames.slice(0).sort();

    // sort note quest to the end
    questNames.unshift(noteQuestName);
    sortedQuestNames.push(noteQuestName);

    questFiles = await getFiles(resolve(sourceDirectory, 'quests/'));
    strings = await getStrings(resolve(projectDirectory, 'app/src/main/res/values/strings.xml'));

    const quests = await Promise.all(sortedQuestNames.map(name => getQuest(name)));

    await writeMarkdownFile(quests);
})().catch(error => {
    console.error(error);
    process.exit(1);
});


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
 * @typedef {object} Quest
 * @property {string} name - The quest's name
 * @property {string} icon - An absolute path to the quest's SVG icon.
 * @property {string} filePath - An absolute path to the quest's Kotlin file.
 * @property {string} title - The quest's title.
 * @property {number} defaultPriority - The quest's default priority (1 is highest priority).
 * @property {string|undefined} firstRelease - The git tag of the first release that included this quest, or undefined if it's unreleased.
 */


/**
 * @param {string} questName
 * @returns {Quest} All information about the quest with the given name.
 */
async function getQuest(questName) {
    const filePath = getQuestFilePath(questName);
    const questFileContent = await readFile(filePath, 'utf8');

    const titleStringName = getQuestTitleStringName(questName, questFileContent);

    return {
        name: questName,
        icon: await getQuestIcon(questName, questFileContent),
        filePath,
        title: strings[titleStringName].replace(/%s/g, '…'),
        defaultPriority: questNames.indexOf(questName) + 1,
        firstRelease: getReleaseVersion(filePath),
    };
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
 * @returns {Promise<string>} The absolute path of the quest's SVG icon.
 */
async function getQuestIcon(questName, questFileContent) {
    const [iconName] = questFileContent.match(/(?<=override val icon = R.drawable.ic_quest_)\w+/) ?? [];

    if (!iconName) {
        throw new Error(`Could not find the icon reference for quest '${questName}'.`);
    }

    const svgFileName = resolve(iconsDirectory, iconName + '.svg');

    try {
        await readFile(svgFileName);
        return svgFileName;
    }
    catch {
        throw new Error(`Could not find the SVG for icon '${iconName}' (quest '${questName}').`);
    }
}


/**
 * @param {string} questName
 * @param {string} questFileContent
 * @returns {string} The Android String Resource name of the quest's title.
 */
function getQuestTitleStringName(questName, questFileContent) {
    let stringResourceNames = questFileContent.match(/(?<=R\.string\.)quest_\w+/g);

    if (stringResourceNames.length === 0) {
        throw new Error(`Could not find the title string reference for quest '${questName}'.`);
    }

    if (stringResourceNames.length === 1) {
        return stringResourceNames[0];
    }

    // heuristic: use the last one that contains "title"
    return stringResourceNames.filter(name => name.includes('title')).pop();
}


/**
 * @param {string} questFilePath
 * @returns {string|undefined} The git tag of the first release that included this file, or undefined if it's unreleased.
 */
function getReleaseVersion(questFilePath) {
    const firstCommit = execFileSync('git', [
        'log',
        '--diff-filter=A',      // only show the commit where the file was added
        '--pretty=format:%H',   // only output the commit hash
        '-1',                   // limit to one commit
        '--',
        questFilePath,
    ], {encoding: 'utf8'});

    const gitTags = execFileSync('git', ['tag', '--contains', firstCommit], {encoding: 'utf8'});

    return gitTags
        .split('\n')
        .filter(tag => /^v\d+\.\d+$/.test(tag)) // exclude empty strings, beta versions and tags missing the `v` prefix
        .sort((a, b) => a.localeCompare(b))
        .shift();
}


/**
 * @param {Quest[]} quests
 */
async function writeMarkdownFile(quests) {
    const markdownFileDirectory = dirname(markdownFilePath);
    const scriptName = relative(projectDirectory, scriptFilePath);

    const markdownLines = [
        `<!-- this file is automatically generated by ${scriptName} -->`,
        '',
        '### StreetComplete quests',
        '',
        'This quest list is automatically updated when new commits are pushed to the `master` branch.',
        '',
        'See also the [quest list in the OSM Wiki](https://wiki.openstreetmap.org/wiki/StreetComplete/Quests).',
        'It is maintained by the OSM Wiki editors and thus may be a bit outdated, but it contains a lot more human-readable information.',
        '',
        '<table>',
        '  <thead>',
        '    <tr>',
        '      <th>Icon</th>',
        '      <th>Quest Name</th>',
        '      <th>Question</th>',
        '      <th>Default Priority</th>',
        '      <th>Since at least version <sup><a href="#user-content-footnote-1">[1]</a></sup></th>',
        '    </tr>',
        '  </thead>',
        '  <tbody>',

        ...quests.flatMap(quest => {
            const relativeIconPath = relative(markdownFileDirectory, quest.icon);
            const relativeFilePath = relative(markdownFileDirectory, quest.filePath);

            const releaseLink = quest.firstRelease
                ? `<a href="https://github.com/streetcomplete/StreetComplete/releases/tag/${quest.firstRelease}">${quest.firstRelease}</a>`
                : 'unreleased';

            return [
                '    <tr>',
                `      <td><img src="${relativeIconPath}"></td>`,
                `      <td><a href="${relativeFilePath}">${quest.name}</a></td>`,
                `      <td>${quest.title}</td>`,
                `      <td>${quest.defaultPriority}</td>`,
                `      <td>${releaseLink}</td>`,
                '    </tr>',
            ];
        }),

        '  </tbody>',
        '</table>',
        '',
        '**<a id="user-content-footnote-1">[1]</a>**: This is automatically detected based on current files\' git tags. Unfortunately, git often gets confused when files are renamed, so a quest may have been introduced (in a different file) before the version displayed here.',
    ];

    await writeFile(markdownFilePath, markdownLines.join('\n'));
}
