# SQLCipher Community Edition Dependency Policy

## Decision

SQLCipher Community Edition is the only non-platform, non-Google runtime dependency approved to
access Toolly's persistent local-vault database.

This approval is narrow:

- Community Edition only;
- no SQLCipher Commercial, Enterprise, trial, hosted or support service;
- no Zetetic account, API key, licence key or runtime network connection;
- no document, metadata, key material or diagnostic data sent to Zetetic;
- no direct SQLCipher types outside the Android vault adapter;
- no plaintext page assets stored in the SQLCipher database;
- no claim that SQLCipher alone completes Toolly's encrypted vault.

The approved Android package is `net.zetetic:sqlcipher-android`. The deprecated
`net.zetetic:android-database-sqlcipher` package is prohibited.

## Why the exception exists

Android Room and platform SQLite do not provide transparent full-database encryption. Building a
database encryption engine inside Toolly would introduce custom cryptographic and storage code
with a substantially larger security and long-term maintenance risk.

SQLCipher runs in the Toolly application process and encrypts the local SQLite database. It is a
library, not a cloud processor. The Community Edition is available under a BSD-style licence and
requires user-accessible attribution.

## Privacy controls

1. SQLCipher receives a random database passphrase only in process.
2. The passphrase is wrapped by a non-exportable Android Keystore key.
3. The passphrase, keys, database paths and SQL statements are never logged.
4. Java client logging is disabled before the database is opened.
5. Firebase, Zetetic, support staff and analytics receive no passphrase or plaintext vault data.
6. Android backup is disabled and the database/key envelope live under `noBackupFilesDir`.

## Supply-chain controls

- Pin the exact SQLCipher version in the Gradle version catalog.
- Resolve it only from the approved repository.
- Commit dependency locks and SHA-256 verification metadata.
- Record licence, transitive dependencies, binary size and CVE review in the dependency registry.
- Generate an SBOM and provenance for release builds.
- Reject dynamic versions, unverified binaries and the deprecated Android package.
- Re-review quarterly and before every production dependency upgrade.

## Twenty-year portability controls

No external library can be guaranteed for twenty years. Toolly therefore treats SQLCipher as a
replaceable adapter:

- domain, use-case and UI modules never import SQLCipher APIs;
- `VaultMetadataStore` owns the persistence boundary;
- every schema version has retained migration and restore fixtures;
- exports use Toolly-owned canonical models, never raw database rows;
- upgrades use dual-read/write-new or export/re-encrypt/import migration;
- a documented plaintext-in-memory export path is test-only and never writes an unencrypted
  migration database;
- the source licence and required notices are retained with each released version;
- if maintenance, security, licence or platform compatibility becomes unacceptable, new writes
  move to a replacement adapter while existing vaults remain readable during migration.

## Release gates

SQLCipher remains a candidate until all of the following pass:

- correct-key reopen and migration;
- wrong-key rejection;
- database-header confidentiality check;
- tamper, truncation and corruption handling;
- Android Keystore loss/invalidation handling without silent reset;
- process-death and low-storage recovery;
- 16 KB Android page-size compatibility;
- representative-device performance and memory benchmarks;
- licence/notice verification;
- qualified security review.

This policy does not approve encrypted cloud backup. Asset encryption and cloud recovery remain
gated by ADR-0007.
