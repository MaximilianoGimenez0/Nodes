import {onDocumentCreated} from "firebase-functions/v2/firestore";
import {onCall, HttpsError} from "firebase-functions/v2/https";
import * as admin from "firebase-admin";

admin.initializeApp();
const db = admin.firestore();

export const sendChatNotification = onDocumentCreated(
  {
    document: "workspaces/{workspaceId}/entries/{entryId}",
    region: "us-central1",
  },
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;

    const newMessage = snapshot.data();
    const workspaceId = event.params.workspaceId;
    const senderUid = newMessage.userId;
    const entryType = newMessage.type;
    const senderName = newMessage.name || "Alguien";

    let notificationBody: string;
    switch (entryType) {
    case "MESSAGE":
      notificationBody = newMessage.value || "Nuevo mensaje";
      break;
    case "CHECK_LIST":
      notificationBody = "Se agregó una nueva lista de tareas";
      break;
    case "LOCALIZATION":
      notificationBody = "Se compartió una localización";
      break;
    case "IMAGE":
      notificationBody = "Se envió una imagen";
      break;
    case "REMINDER":
      notificationBody = "Se creó un recordatorio";
      break;
    default:
      notificationBody = "Se actualizó el workspace";
      break;
    }

    const workspaceDoc = await db
      .collection("workspaces")
      .doc(workspaceId)
      .get();
    if (!workspaceDoc.exists) return;

    const workspaceData = workspaceDoc.data();
    const memberUids: string[] = workspaceData?.members || [];
    const recipientUids = memberUids.filter((uid) => uid !== senderUid);

    if (recipientUids.length === 0) return;

    const allTokens: string[] = [];
    for (const uid of recipientUids) {
      const tokenSnapshot = await db
        .collection("users")
        .doc(uid)
        .collection("tokens")
        .get();
      tokenSnapshot.forEach((tokenDoc) => {
        const tokenData = tokenDoc.data();
        if (tokenData.token) allTokens.push(tokenData.token);
      });
    }

    if (allTokens.length === 0) return;

    const workspaceName = workspaceData?.name || "Workspace";
    const title = `${senderName} mandó un nuevo mensaje en ${workspaceName}`;

    try {
      await admin.messaging().sendEachForMulticast({
        tokens: allTokens,
        data: {
          title,
          body: notificationBody,
          workspaceId,
          sender: senderUid,
        },
      });
    } catch (error) {
      console.error("Error al enviar notificación:", error);
    }
  }
);

export const deleteUserAccount = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError(
      "unauthenticated",
      "El usuario debe estar autenticado para realizar esta acción."
    );
  }

  const uid = request.auth.uid;
  const bucket = admin.storage().bucket();

  try {
    const workspacesSnapshot = await db.collection("workspaces")
      .where("members", "array-contains", uid)
      .get();

    const workspaceDeletions = workspacesSnapshot.docs.map(async (doc) => {
      const workspaceId = doc.id;
      const workspaceData = doc.data();

      if (workspaceData.ownerId === uid) {
        // Firestore no elimina subcolecciones en cascada; requiere borrado explícito de 'entries'
        const entriesSnapshot = await doc.ref.collection("entries").get();
        const deletePromises = entriesSnapshot.docs
          .map((entry) => entry.ref.delete());
        await Promise.all(deletePromises);

        await doc.ref.delete();

        // Ignorar fallos de Storage si el prefijo de imágenes no existe
        await bucket.deleteFiles({prefix: `node_pictures/${workspaceId}/`})
          .catch(() => console.log(
            "Directorio vacío o inexistente en Storage"
          ));
      } else {
        await doc.ref.update({
          members: admin.firestore.FieldValue.arrayRemove(uid),
        });

        const myEntriesSnapshot = await db
          .collection(`workspaces/${workspaceId}/entries`)
          .where("userId", "==", uid)
          .get();

        const deleteMemberPromises = myEntriesSnapshot.docs
          .map((entry) => entry.ref.delete());
        await Promise.all(deleteMemberPromises);
      }
    });

    await Promise.all(workspaceDeletions);

    const tokensSnapshot = await db
      .collection("users")
      .doc(uid)
      .collection("tokens")
      .get();

    const deleteTokensPromises = tokensSnapshot.docs
      .map((token) => token.ref.delete());
    await Promise.all(deleteTokensPromises);

    await db.collection("users").doc(uid).delete();

    // Ignorar fallos de Storage si el usuario no posee foto de perfil
    await bucket.deleteFiles({prefix: `profile_pictures/${uid}/`})
      .catch(() => console.log(
        "Directorio de perfil vacío o inexistente"
      ));

    await admin.auth().deleteUser(uid);

    return {result: "Cuenta eliminada exitosamente"};
  } catch (error) {
    console.error("Error al eliminar cuenta:", error);
    const errorMessage = (error as Error)?.message ||
      "Error interno al procesar la eliminación.";
    throw new HttpsError("internal", errorMessage);
  }
});
