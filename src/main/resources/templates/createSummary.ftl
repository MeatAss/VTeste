<#import "parts/common.ftl" as common>

<#assign headerContent>
     <link rel="stylesheet" href="../static/styles/animateLoading.css">

    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>

    <script src="/webjars/stomp-websocket/2.3.3/stomp.min.js"></script>
    <script src="../static/js/createSummaryWebService.js"></script>
    <script src="../static/js/createSummaryApp.js"></script>
    <script src="../static/js/createSummaryDragAndDrop.js"></script>

    <link rel="stylesheet" href="https://cdn.jsdelivr.net/simplemde/latest/simplemde.min.css">
    <script src="https://cdn.jsdelivr.net/simplemde/latest/simplemde.min.js"></script>

    <script src="../static/js/markdownEditor.js"></script>


    <style>
        #tagsUL > li:hover {
            background-color: #e6e6e6;
        }
    </style>
</#assign>

<@common.common header=headerContent>
    <div class="m-2 w-50 h-50">
        <div class="input-group mb-3">
            <input id="searchText" type="search"  class="form-control" placeholder="Поиск" aria-label="Recipient's username" aria-describedby="button-addon2">
            <div class="input-group-append">
                <button class="btn btn-outline-secondary" type="button" id="button-addon2" onclick="sendSearchData()">
                    <i class="fas fa-search"></i>
                </button>
            </div>
        </div>
    </div>

    <div class="mx-auto border p-4 mt-2" style="width: 50%; height: 50%;">
        <form id="addSummaryForm">
            <div class="form-group">
                <label for="nameSummary">Название</label>
                <input class="form-control has-error" id="nameSummary" type="text" placeholder="Введите название конспекта" required maxlength="100">
            </div>
            <div class="form-group">
                <label for="shortDescription">Краткое описание</label>
                <input class="form-control" id="shortDescription" type="text" placeholder="Краткое описание" required maxlength="200">
            </div>
            <div class="form-group">
                <label for="specialtyNumber">Номер специальности</label>
                <input class="form-control" id="specialtyNumber" type="text" placeholder="Номер специальности" required maxlength="50">
            </div>
            <div class="form-group position-relative">
                <label for="sumaryTags">Тэги</label>
                <input class="form-control" id="sumaryTags" type="text" placeholder="Тэги" required maxlength="200">
                <ul id="tagsUL" class="position-absolute border nav flex-column bg-light invisible" style="z-index: 999; width: 100%">
                </ul>
            </div>
            <div class="form-group" id="divTextId">
                <label for="textSummary">Текст</label>
                <textarea class="form-control" name="desc" id="textSummary" required minlength="25" maxlength="1000"></textarea>
                <form method="POST" enctype="multipart/form-data" id="fileUploadForm">

                </form>
            </div>

            <div class="form-group" id="divLoaderImage">

            </div>

            <button id="submit" class="btn btn-primary" onclick="sendData()">Submit</button>
        </form>
    </div>
</@common.common>