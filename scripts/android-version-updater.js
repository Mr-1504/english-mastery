const versionNameRegex = /versionName\s+"([^"]+)"/;
const versionCodeRegex = /versionCode\s+(\d+)/;

module.exports.readVersion = function (contents) {
  const match = contents.match(versionNameRegex);
  return match ? match[1] : null;
};

module.exports.writeVersion = function (contents, version) {
  if (!versionNameRegex.test(contents) || !versionCodeRegex.test(contents)) {
    throw new Error('Could not find versionName/versionCode in app/build.gradle');
  }
  let updated = contents.replace(versionNameRegex, `versionName "${version}"`);
  updated = updated.replace(versionCodeRegex, (_, code) => `versionCode ${parseInt(code, 10) + 1}`);
  return updated;
};
