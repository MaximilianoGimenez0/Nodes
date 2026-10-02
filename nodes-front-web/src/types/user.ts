export interface UserProfile {
  id: string; // UID del usuario
  email: string;
  firstName: string;
  lastName: string;
  profilePicture: string; // URL alojada en Firebase Storage (/profile_pictures)
}
