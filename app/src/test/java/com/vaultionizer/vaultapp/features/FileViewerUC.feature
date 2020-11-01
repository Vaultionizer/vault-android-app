Feature: FileViewer
  Scenario Outline: A user selects a file
    Given a user was logged in successfully
    And the user has opened the FileViewer
    And a list with all selected files exists <IN_2beDownloaded>
    And the user's client has decrypted the given RefFile
    When the user clicks on a file
    Then the file gets selected <file>
    And the file is appended onto the list of files that should the downloaded <OUT_2beDownloaded>
    Examples:
      |  IN_2beDownloaded    | file     | OUT_2beDownloaded       |
      | ~empty~              | text.txt | "text.txt"              |
      | "text.txt"           | key.dbkx | "test.txt", "key.kdbx"  |

  Scenario Outline: A user deselects a file
    Given a user was logged in successfully
    And the user has opened the FileViewer
    And the user's client has decrypted the given RefFile
    And at least one file is selected <IN_2beDownloaded>
    When a the user clicks on the file <file>
    And the file is selected
    Then the file is deselected
    And the file is removed from the <OUT_2beDownloaded>
    Examples:
      |  IN_2beDownloaded       | file     | OUT_2beDownloaded |
      | "test.txt", "key.kdbx"  | key.kdbx | "test.txt"        |
      | "test.txt", "key.kdbx"  | test.txt | "key.kdbx"        |
      | "key.kdbx"              | key.kdbx | ~empty~           |


  Scenario Outline: A user downloads a file
    Given a user was logged in successfully
    And the user has opened the FileViewer
    And the user's client has decrypted the given RefFile
    When the user clicks on the "Download"-button
    And at least one file is selected
    Then the client sends a download request with <2beDownloaded> to the server
    Examples:
      | 2beDownloaded                              |
      | "test.txt", "key.kdbx"                     |
      | "key.kdbx"                                 |
      | "test1.txt","test2.txt", "video008.mp4"    |

  Scenario Outline: A user uploads a file
    Given a user was logged in successfully
    And the user has opened the FileViewer
    And the user's client has decrypted the given RefFile
    And no file is selected
    When the user clicks the "Upload"-button
    Then the native file explorer is opened
    And all chosen files are return with their path to the application <2beUploaded>
    And the clients sends a upload request with the returned files to the server
    Examples:
      | 2beUploaded                                                                                                                                     |
      |  "storage://dcim/pictures/pic_001", "storage://dcim/videos/video008.mp4", "storage://db/keybases/key.kdbx", "storage://dcim/pictures/pic_002",  |

    @TODO
    # save file